package com.github.storytime.service;

import com.github.storytime.builder.StatementRequestBuilder;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.exception.PbSignatureException;
import com.github.storytime.mapper.PbStatementMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.statement.request.Request;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import io.micrometer.core.instrument.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CARD_LAST_DIGITS;
import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.right;

@Service
public class PbStatementsService {

    private static final int ONE = 1;
    private static final Logger LOGGER = LogManager.getLogger(PbStatementsService.class);

    private final RestTemplate restTemplate;
    private final DateService dateService;
    private final AdditionalCommentService additionalCommentService;
    private final CustomConfig customConfig;
    private final StatementRequestBuilder statementRequestBuilder;
    private final PbStatementMapper pbStatementMapper;
    private final MerchantService merchantService;
    private final Timer pbRequestTimeTimer;

    @Autowired
    public PbStatementsService(final RestTemplate restTemplate,
                               final CustomConfig customConfig,
                               final PbStatementMapper pbStatementMapper,
                               final MerchantService merchantService,
                               final Timer pbRequestTimeTimer,
                               final StatementRequestBuilder statementRequestBuilder,
                               final AdditionalCommentService additionalCommentService,
                               final DateService dateService) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
        this.pbStatementMapper = pbStatementMapper;
        this.pbRequestTimeTimer = pbRequestTimeTimer;
        this.merchantService = merchantService;
        this.statementRequestBuilder = statementRequestBuilder;
        this.additionalCommentService = additionalCommentService;
        this.dateService = dateService;
    }


    public CompletableFuture<List<Statement>> getPbTransactions(final AppUser u, final MerchantInfo m) {

        final Duration period = ofMillis(m.getSyncPeriod());
        final ZonedDateTime startDate = dateService.millisUserDate(m.getSyncStartDate(), u);
        final ZonedDateTime now = now().withZoneSameInstant(of(u.getTimeZone()));
        final ZonedDateTime endDate = between(startDate, now).toMillis() < m.getSyncPeriod() ? now : startDate.plus(period);

        LOGGER.info("Syncing u: {} mId: {} mNumb: {} sd: {} lastSync: {} card: {}",
                u.getId(),
                m.getId(),
                m.getMerchantId(),
                dateService.toIsoFormat(startDate),
                dateService.toIsoFormat(endDate),
                right(m.getCardNumber(), CARD_LAST_DIGITS)
        );

        final Request requestToBank = statementRequestBuilder.buildStatementRequest(m.getMerchantId(),
                m.getPassword(),
                dateService.toPbFormat(startDate),
                dateService.toPbFormat(endDate),
                m.getCardNumber()
        );

        return supplyAsync(pullAndHandlePbRequest(u, m, startDate, endDate, requestToBank));
    }

    private Supplier<List<Statement>> pullAndHandlePbRequest(final AppUser u,
                                                             final MerchantInfo m,
                                                             final ZonedDateTime startDate,
                                                             final ZonedDateTime endDate,
                                                             final Request requestToBank) {
        return () -> pullPbTransactions(requestToBank)
                .map(b -> handleResponse(u, m, startDate, endDate, b))
                .orElse(emptyList());
    }

    private List<Statement> handleResponse(final AppUser u,
                                           final MerchantInfo m,
                                           final ZonedDateTime startDate,
                                           final ZonedDateTime endDate,
                                           final ResponseEntity<String> body) {
        try {
            final List<Statement> allPbTransactions = pbStatementMapper.mapRequestBody(body);
            final List<Statement> onlyNewPbTransactions = filterNewPbTransactions(startDate, endDate, allPbTransactions, u);

            additionalCommentService.handle(onlyNewPbTransactions, m, u.getTimeZone());
            m.setSyncStartDate(endDate.toInstant().toEpochMilli()); // later will do save to update last sync time
            return onlyNewPbTransactions;
        } catch (PbSignatureException e) {
            // roll back for one day
            final long rollBackStartDate = startDate.minusHours(ONE).toInstant().toEpochMilli();
            LOGGER.error("Invalid signature, going to roll back from: {} to: {}",
                    dateService.toIsoFormat(startDate),
                    dateService.toIsoFormat(rollBackStartDate, u));
            merchantService.save(m.setSyncStartDate(rollBackStartDate));
            return emptyList();
        }
    }


    public Optional<ResponseEntity<String>> pullPbTransactions(final Request requestToBank) {
        try {
            final String pbTransactionsUrl = customConfig.getPbTransactionsUrl();
            LOGGER.debug("Going to call: {}", pbTransactionsUrl);
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ResponseEntity<String>> response = Optional.of(restTemplate.postForEntity(pbTransactionsUrl, requestToBank, String.class));
            st.stop();
            LOGGER.debug("Receive bank response, execution time: {} sec", st.getTotalTimeSeconds());
            pbRequestTimeTimer.record(st.getTotalTimeMillis(), TimeUnit.MILLISECONDS);
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot do bank request: {}", e.getMessage());
            return empty();
        }
    }

    public List<Statement> filterNewPbTransactions(ZonedDateTime start, ZonedDateTime end, List<Statement> pbStatements, AppUser appUser) {
        final Comparator<ZonedDateTime> comparator = comparing(zdt -> zdt.truncatedTo(MILLIS));
        // sometimes new transactions can be available with delay, so we need to change start time of filtering
        final ZonedDateTime searchStartTime = start.minus(customConfig.getFilterTimeMillis(), MILLIS);
        return pbStatements
                .stream()
                .filter(t -> {
                    final ZonedDateTime tTime = dateService.xmlDateTimeToZoned(t.getTrandate(), t.getTrantime(), appUser.getTimeZone());
                    return comparator.compare(searchStartTime, tTime) <= 0 && comparator.compare(end, tTime) > 0;
                }).collect(toList());
    }

}
