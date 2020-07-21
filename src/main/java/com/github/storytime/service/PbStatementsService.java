package com.github.storytime.service;

import com.github.storytime.builder.PbRequestBuilder;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.error.exception.PbSignatureException;
import com.github.storytime.mapper.response.PbStatementResponseMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.pb.jaxb.request.Request;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.http.PbStatementsHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CARD_LAST_DIGITS;
import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.error.AsyncErrorHandlerUtil.getPbServiceAsyncHandler;
import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbStatementsService {

    private static final Logger LOGGER = LogManager.getLogger(PbStatementsService.class);

    private final DateService dateService;
    private final PbStatementsHttpService pbStatementsHttpService;
    private final AdditionalCommentService additionalCommentService;
    private final CustomConfig customConfig;
    private final PbRequestBuilder pbRequestBuilder;
    private final PbStatementResponseMapper pbStatementResponseMapper;
    private final MerchantService merchantService;
    private final Executor cfThreadPool;


    @Autowired
    public PbStatementsService(
            final CustomConfig customConfig,
            final PbStatementResponseMapper pbStatementResponseMapper,
            final MerchantService merchantService,
            final Executor cfThreadPool,
            final PbRequestBuilder statementRequestBuilder,
            final AdditionalCommentService additionalCommentService,
            final DateService dateService,
            final PbStatementsHttpService pbStatementsHttpService) {
        this.customConfig = customConfig;
        this.pbStatementResponseMapper = pbStatementResponseMapper;
        this.merchantService = merchantService;
        this.cfThreadPool = cfThreadPool;
        this.pbRequestBuilder = statementRequestBuilder;
        this.additionalCommentService = additionalCommentService;
        this.dateService = dateService;
        this.pbStatementsHttpService = pbStatementsHttpService;
    }

    public CompletableFuture<List<Statement>> getPbTransactions(final AppUser u, final MerchantInfo m) {

        final Duration period = ofMillis(m.getSyncPeriod());
        final ZonedDateTime startDate = dateService.millisUserDate(m.getSyncStartDate(), u);
        final ZonedDateTime now = now().withZoneSameInstant(of(u.getTimeZone()));
        final ZonedDateTime endDate = between(startDate, now).toMillis() < m.getSyncPeriod() ? now : startDate.plus(period);

        LOGGER.info("Syncing u:[{}] desc:[{}] mId:[{}] mNumb:[{}] sd:[{}] lastSync:[{}] card:[{}]",
                u.getId(),
                ofNullable(m.getShortDesc()).orElse(EMPTY),
                m.getId(),
                m.getMerchantId(),
                dateService.millisToIsoFormat(startDate),
                dateService.millisToIsoFormat(endDate),
                right(m.getCardNumber(), CARD_LAST_DIGITS)
        );

        final Request requestToBank = pbRequestBuilder.buildStatementRequest(m, dateService.toPbFormat(startDate), dateService.toPbFormat(endDate));
        final Supplier<List<Statement>> pullPbTransactionsSupplier = () -> pbStatementsHttpService.pullPbTransactions(requestToBank)
                .map(b -> handleResponse(u, m, startDate, endDate, b))
                .orElse(emptyList());

        return supplyAsync(pullPbTransactionsSupplier, cfThreadPool)
                .thenApply(sList -> sList
                        .stream()
                        .peek(s -> additionalCommentService.handle(s, m, u.getTimeZone()))
                        .collect(toUnmodifiableList()))
                .handle(getPbServiceAsyncHandler());
    }


    private List<Statement> handleResponse(final AppUser u,
                                           final MerchantInfo m,
                                           final ZonedDateTime startDate,
                                           final ZonedDateTime endDate,
                                           final ResponseEntity<String> body) {
        try {
            final List<Statement> allPbTransactions = pbStatementResponseMapper.mapStatementRequestBody(body);
            final List<Statement> onlyNewPbTransactions = filterNewPbTransactions(startDate, endDate, allPbTransactions, u);
            m.setSyncStartDate(endDate.toInstant().toEpochMilli()); // later will do save to update last sync time
            return onlyNewPbTransactions;
        } catch (PbSignatureException e) {
            // roll back for 1 hrs
            final var rollBackStartDateMillis = startDate.minusHours(customConfig.getPbRollBackPeriod()).toInstant().toEpochMilli();
            final var mDesc = ofNullable(m.getShortDesc()).orElse(EMPTY);
            final var mId = m.getMerchantId();
            final var sDate = dateService.millisToIsoFormat(startDate);
            final var rollBackTime = dateService.millisToIsoFormat(rollBackStartDateMillis, u);

            LOGGER.error("Desc:[{}] mId:[{}] invalid signature, rollback from:[{}] to:[{}]", mDesc, mId, sDate, rollBackTime);

            if (rollBackStartDateMillis > (now().toInstant().toEpochMilli() - customConfig.getMaxRollbackPeriod())) {
                merchantService.save(m.setSyncStartDate(rollBackStartDateMillis));
            } else {
                LOGGER.error("Desc:[{}] mId:[{}] invalid signature, failed to rollback from:[{}] to:[{}] date is too big", mDesc, mId, sDate, rollBackTime);
            }

            return emptyList();
        }
    }


    public List<Statement> filterNewPbTransactions(final ZonedDateTime start,
                                                   final ZonedDateTime end,
                                                   final List<Statement> pbStatements,
                                                   final AppUser appUser) {
        final Comparator<ZonedDateTime> comparator = comparing(zdt -> zdt.truncatedTo(MILLIS));
        // sometimes new transactions can be available with delay, so we need to change start time of filtering
        final ZonedDateTime searchStartTime = start.minus(customConfig.getFilterTimeMillis(), MILLIS);
        return pbStatements
                .stream()
                .filter(getStatementComparatorPredicate(end, appUser, comparator, searchStartTime))
                .collect(toUnmodifiableList());
    }

    public Predicate<Statement> getStatementComparatorPredicate(final ZonedDateTime end,
                                                                final AppUser appUser,
                                                                final Comparator<ZonedDateTime> comparator,
                                                                final ZonedDateTime searchStartTime) {
        return t -> {
            final ZonedDateTime tTime = dateService.xmlDateTimeToZoned(t.getTrandate(), t.getTrantime(), appUser.getTimeZone());
            return comparator.compare(searchStartTime, tTime) <= 0 && comparator.compare(end, tTime) > 0;
        };
    }
}
