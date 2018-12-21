package com.github.storytime.service;

import com.github.storytime.function.OnSuccess;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.ExpiredPbStatement;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static com.github.storytime.error.AsyncErrorHandlerUtil.getZenDiffUpdateHandler;
import static com.github.storytime.function.FunctionUtils.logAndGetEmptyForSync;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.logging.log4j.Level.WARN;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);

    private final MerchantService merchantService;
    private final PbStatementsService pbStatementsService;
    private final UserService userService;
    private final ZenDiffService zenDiffService;
    private final PbToZenMapper pbToZenMapper;
    private final Executor cfThreadPool;
    private final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction;

    @Autowired
    public PbSyncService(final MerchantService merchantService,
                         final PbStatementsService pbStatementsService,
                         final UserService userService,
                         final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction,
                         final ZenDiffService zenDiffService,
                         final Executor cfThreadPool,
                         final PbToZenMapper pbToZenMapper) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.zenDiffService = zenDiffService;
        this.cfThreadPool = cfThreadPool;
        this.alreadyMappedPbZenTransaction = alreadyMappedPbZenTransaction;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
    }

    @Async
    public void sync(final Function<MerchantService, Optional<List<MerchantInfo>>> selectFunction) {
        userService.findAll()
                .forEach(user -> selectFunction.apply(merchantService)
                        .map(merchantLists -> of(merchantLists
                                .stream()
                                .map(merchantInfo -> pbStatementsService.getPbTransactions(user, merchantInfo)) // create async requests
                                .collect(toUnmodifiableList()))
                                .flatMap(cfList -> of(allOf(cfList.toArray(new CompletableFuture[merchantLists.size()])) // wait for completions of all requests
                                        .thenApply(aVoid -> cfList.stream().map(CompletableFuture::join).collect(toUnmodifiableList())) // collect results
                                        .thenAccept(newPbDataList -> handlePbCfRequestData(user, merchantLists, newPbDataList))))) // process all data
                        .or(logAndGetEmptyForSync(LOGGER, WARN, "No merchants to sync")));
    }

    public void handlePbCfRequestData(final AppUser user,
                                      final List<MerchantInfo> merchants,
                                      final List<List<Statement>> newPbDataList) {

        final List<ExpiredPbStatement> maybePushed = newPbDataList
                .stream()
                .flatMap(Collection::stream)
                .map(ExpiredPbStatement::new)
                .filter(not(alreadyMappedPbZenTransaction::contains))
                .collect(toUnmodifiableList());

        if (maybePushed.isEmpty()) {
            ifNotExists(user, merchants);
        } else {
            LOGGER.info("User:[{}] has:[{}] transactions sync period", user.getId(), maybePushed.size());
            final OnSuccess onSuccess = () -> {
                alreadyMappedPbZenTransaction.addAll(maybePushed);
                merchantService.saveAll(merchants);
            };
            doUpdateZenInfoRequest(user, newPbDataList, onSuccess);
        }
    }

    public void ifNotExists(final AppUser user, final List<MerchantInfo> merchants) {
        LOGGER.info("No new transaction for user:[{}] Nothing to push in current sync thread", user.getId());
        merchantService.saveAll(merchants);
    }

    private void doUpdateZenInfoRequest(final AppUser appUser,
                                        final List<List<Statement>> newPbData,
                                        final OnSuccess onSuccess) {
        // step by step in one thread
        supplyAsync(() -> zenDiffService.getZenDiffByUser(appUser), cfThreadPool)
                .thenApply(zenDiffResponse -> zenDiffResponse
                        .flatMap(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbData, zenDiff, appUser)))
                .thenApply(zenDiffRequest -> zenDiffRequest
                        .flatMap(zr -> zenDiffService.pushToZen(appUser, zr)))
                .thenApply(zenResponse -> zenResponse
                        .flatMap(zr -> userService.updateUserLastZenSyncTime(appUser.setZenLastSyncTimestamp(zr.getServerTimestamp()))))
                .thenAccept(au -> onSuccess.commit())
                .handle(getZenDiffUpdateHandler());

    }
}
