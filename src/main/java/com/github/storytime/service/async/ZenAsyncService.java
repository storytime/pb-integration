package com.github.storytime.service.async;

import com.github.storytime.function.ZenDiffLambdaHolder;
import com.github.storytime.model.api.ms.AppUser;
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
public class ZenAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(ZenAsyncService.class);

    private final Executor pool;
    private final ZenDiffHttpService zenDiffHttpService;
    private final ZenDiffLambdaHolder zenDiffLambdaHolder;

    @Autowired
    public ZenAsyncService(final ZenDiffHttpService zenDiffHttpService,
                           final ZenDiffLambdaHolder zenDiffLambdaHolder,
                           final Executor cfThreadPool) {
        this.zenDiffHttpService = zenDiffHttpService;
        this.pool = cfThreadPool;
        this.zenDiffLambdaHolder = zenDiffLambdaHolder;
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForPb(final AppUser appUser) {
        LOGGER.debug("Fetching all ZEN data for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getInitialFunction(appUser)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForSavings(final AppUser appUser) {
        LOGGER.debug("Fetching ZEN accounts/instruments for savings for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getSavingsFunction(appUser)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForReconcile(final AppUser appUser,
                                                                              long startDate) {
        LOGGER.debug("Fetching ZEN accounts/tags ynab for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getAccountAndTags(appUser, startDate)), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> zenDiffByUserForPbAccReconcile(final AppUser appUser,
                                                                                   long startDate) {
        LOGGER.debug("Fetching ZEN accounts for user: [{}] - start", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getAccount(appUser, startDate)), pool);
    }

    public CompletableFuture<YnabToZenSyncHolder> zenDiffByUserForYnab(final AppUser appUser,
                                                                       final long clientSyncTime,
                                                                       final YnabSyncConfig config) {
        LOGGER.debug("Fetching ZEN diff for YNAB budget config: [{}], last sync [{}], tags method [{}] - start", config.getBudgetName(), config.getLastSync(), config.getTagsSyncProperties());
        return supplyAsync(() -> new YnabToZenSyncHolder(zenDiffHttpService.getZenDiffByUser(zenDiffLambdaHolder.getYnabFunction(appUser, clientSyncTime, config)), config), pool);
    }

    public CompletableFuture<Optional<ZenResponse>> pushToZen(final AppUser appUser,
                                                              final ZenDiffRequest request) {
        LOGGER.debug("Pushing zen diff for user: [{}] - stared", appUser.getId());
        return supplyAsync(() -> zenDiffHttpService.pushToZen(appUser, request), pool);
    }
}
