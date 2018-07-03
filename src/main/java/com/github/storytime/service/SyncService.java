package com.github.storytime.service;

import com.github.storytime.builder.HistoryRequestBuilder;
import com.github.storytime.exception.PbSignatureException;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.history.request.Request;
import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.model.zen.ZenDiffRequest;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.time.Duration.between;
import static java.time.Duration.ofMillis;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class SyncService {

    private static final Logger LOGGER = getLogger(SyncService.class);
    private static final int ONE_DAY = 1;

    private final MerchantService merchantService;
    private final HistoryRequestBuilder historyRequestBuilder;
    private final BankHistoryService bankHistoryService;
    private final UserService userService;
    private final ZenDiffService zenDiffService;
    private final PbToZenMapper pbToZenMapper;
    private final DateService dateService;
    private final AdditionalCommentService additionalCommentService;

    @Autowired
    public SyncService(final MerchantService merchantService,
                       final HistoryRequestBuilder historyRequestBuilder,
                       final BankHistoryService bankHistoryService,
                       final UserService userService,
                       final ZenDiffService zenDiffService,
                       final PbToZenMapper pbToZenMapper,
                       final AdditionalCommentService additionalCommentService,
                       final DateService dateService) {
        this.merchantService = merchantService;
        this.historyRequestBuilder = historyRequestBuilder;
        this.userService = userService;
        this.zenDiffService = zenDiffService;
        this.bankHistoryService = bankHistoryService;
        this.pbToZenMapper = pbToZenMapper;
        this.additionalCommentService = additionalCommentService;
        this.dateService = dateService;
    }

    @Async
    public void sync() {
        userService.findAll().forEach(u -> {
            final List<MerchantInfo> merchants = merchantService.getAllEnabledMerchants();
            final List<List<Statement>> newPbDataList = merchants
                    .stream()
                    .map(m -> getPbTransactions(u, m))
                    .collect(toList())
                    .stream()
                    .map(CompletableFuture::join) // get doUpdateZenInfoRequest result
                    .collect(toList());

            final long amountOfNewData = newPbDataList.stream().mapToLong(List::size).sum(); // any new
            if (amountOfNewData > 0) {
                LOGGER.info("User {} has new transactions from bank {}", u.getId(), amountOfNewData);
                doUpdateZenInfoRequest(u, newPbDataList, merchants);
            } else {
                LOGGER.warn("User {} has NO new transactions from bank", u.getId());
            }
        });
    }

    private void doUpdateZenInfoRequest(final User u, final List<List<Statement>> newPbData, final List<MerchantInfo> merchants) {

        supplyAsync(() -> zenDiffService.getZenDiffByUser(u))
                .thenAccept(ozr -> ozr.ifPresent(zenDiff -> {
                            final ZenDiffRequest request = pbToZenMapper
                                    .buildZenReqFromPbData(newPbData, zenDiff, u);
                            zenDiffService.pushToZen(u, request).ifPresent(zr -> merchantService.saveAll(merchants));
                        })
                );
    }


    private CompletableFuture<List<Statement>> getPbTransactions(final User u, final MerchantInfo m) {

        final Duration period = ofMillis(m.getSyncPeriod());
        final ZonedDateTime startDate = dateService.millisUserDate(m.getSyncStartDate(), u);
        final ZonedDateTime now = now().withZoneSameInstant(of(u.getTimeZone()));
        final ZonedDateTime endDate = between(startDate, now).toMillis() < m.getSyncPeriod() ? now : startDate.plus(period);

        LOGGER.info("Syncing u {} m id {} m id: {} sd: {} lastSync: {} card: {}",
                u.getId(),
                m.getId(),
                m.getMerchantId(),
                dateService.toIsoFormat(startDate),
                dateService.toIsoFormat(endDate),
                m.getCardNumber());

        final Request requestToBank = historyRequestBuilder.buildHistoryRequest(m.getMerchantId(),
                m.getPassword(),
                dateService.toPbFormat(startDate),
                dateService.toPbFormat(endDate),
                m.getCardNumber()
        );

        return supplyAsync(pullAndHandlePbRequest(u, m, startDate, endDate, requestToBank));
    }

    private Supplier<List<Statement>> pullAndHandlePbRequest(final User u,
                                                             final MerchantInfo m,
                                                             final ZonedDateTime startDate,
                                                             final ZonedDateTime endDate,
                                                             final Request requestToBank) {
        return () -> bankHistoryService.pullPbTransactions(requestToBank)
                .map(b -> handleResponse(u, m, startDate, endDate, b))
                .orElse(emptyList());
    }

    private List<Statement> handleResponse(User u,
                                           MerchantInfo m,
                                           ZonedDateTime startDate,
                                           ZonedDateTime endDate,
                                           ResponseEntity<String> body) {
        try {
            final List<Statement> allPbTransactions = bankHistoryService
                    .getPbTransactionsFromBody(body);
            final List<Statement> onlyNewPbTransactions = bankHistoryService
                    .filterNewPbTransactions(startDate, endDate, allPbTransactions, u);

            additionalCommentService.handle(onlyNewPbTransactions, m, u.getTimeZone());
            m.setSyncStartDate(endDate.toInstant().toEpochMilli()); // later will do save to update last sync time
            return onlyNewPbTransactions;
        } catch (PbSignatureException e) {
            // roll back for one day
            final long rollBackStartDate = startDate.minusDays(ONE_DAY).toInstant().toEpochMilli();
            LOGGER.error("Invalid signature, going to roll back from: {} to: {}", startDate, rollBackStartDate);
            merchantService.save(m.setSyncStartDate(rollBackStartDate));
            return emptyList();
        }
    }
}
