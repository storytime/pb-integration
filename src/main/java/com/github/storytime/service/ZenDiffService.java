package com.github.storytime.service;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.ynab.YnabToZenSyncHolder;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.service.http.ZenDiffHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class ZenDiffService {

    private static final Logger LOGGER = LogManager.getLogger(ZenDiffService.class);

    private final ZenDiffHttpService zenDiffHttpService;
    private final Executor cfThreadPool;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;

    @Autowired
    public ZenDiffService(final ZenDiffHttpService zenDiffHttpService,
                          final Executor cfThreadPool,
                          final ZenDiffLambdaHolder zenDiffLambdaHolder) {
        this.zenDiffHttpService = zenDiffHttpService;
        this.cfThreadPool = cfThreadPool;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;

    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForPb(final AppUser appUser) {
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getInitialFunction(appUser)), cfThreadPool);
    }

    public Optional<ZenResponse> zenDiffByUserForSavings(final AppUser appUser) {
        return zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getSavingsFunction(appUser));
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForReconcile(final AppUser appUser, long startDate) {
        LOGGER.debug("Fetching ZEN accounts, for user: [{}]", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getAccount(appUser, startDate)), cfThreadPool);
    }

    public CompletableFuture<YnabToZenSyncHolder> zenDiffByUserForYnab(final AppUser appUser,
                                                                       final long clientSyncTime,
                                                                       final YnabSyncConfig config) {
        LOGGER.debug("Calling ZEN diff for YNAB budget config: [{}], last sync [{}], tags method [{}]", config.getBudgetName(), config.getLastSync(), config.getTagsSyncProperties());
        return supplyAsync(() -> getYnabToZenSyncHolder(appUser, clientSyncTime, config), cfThreadPool);
    }

    private YnabToZenSyncHolder getYnabToZenSyncHolder(final AppUser appUser,
                                                       final long clientSyncTime,
                                                       final YnabSyncConfig config) {
        return new YnabToZenSyncHolder(zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getYnabFunction(appUser, clientSyncTime, config)), config);
    }

    public Optional<ZenResponse> pushToZen(final AppUser appUser, final ZenDiffRequest request) {
        return zenDiffHttpService.pushToZen(appUser, request);
    }
}