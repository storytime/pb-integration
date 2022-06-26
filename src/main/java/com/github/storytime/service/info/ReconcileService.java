package com.github.storytime.service.info;

import com.github.storytime.mapper.response.ReconcileCommonMapper;
import com.github.storytime.mapper.zen.ZenCommonMapper;
import com.github.storytime.model.api.PbZenReconcileResponse;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.service.PbAccountService;
import com.github.storytime.service.async.UserAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
import com.github.storytime.service.utils.DateService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.error.AsyncErrorHandlerUtil.logReconcilePbJson;
import static com.github.storytime.service.utils.STUtils.createSt;
import static com.github.storytime.service.utils.STUtils.getTimeAndReset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Component
public class ReconcileService {

    private static final Logger LOGGER = LogManager.getLogger(ReconcileService.class);

    private final UserAsyncService awsUserService;
    private final PbAccountService pbAccountService;
    private final ZenCommonMapper zenCommonMapper;
    private final DateService dateService;
    private final ZenAsyncService zenAsyncService;
    private final ReconcileCommonMapper reconcileCommonMapper;

    @Autowired
    public ReconcileService(
            final UserAsyncService awsUserService,
            final ZenCommonMapper zenCommonMapper,
            final PbAccountService pbAccountService,
            final DateService dateService,
            final ReconcileCommonMapper reconcileCommonMapper,
            final ZenAsyncService zenAsyncService) {
        this.zenAsyncService = zenAsyncService;
        this.zenCommonMapper = zenCommonMapper;
        this.pbAccountService = pbAccountService;
        this.dateService = dateService;
        this.reconcileCommonMapper = reconcileCommonMapper;
        this.awsUserService = awsUserService;
    }

    public CompletableFuture<ResponseEntity<PbZenReconcileResponse>> reconcilePbJson(final String userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Building pb/zen reconcile json, for user: [{}] - stared", userId);
            return getUserAsync(userId)
                    .thenCompose(appUser -> {
                        final var merchantInfos = appUser.getPbMerchant();
                        final var startDate = dateService.getUserStarDateInMillis(appUser);
                        final var pbAccsFuture = pbAccountService.getPbAsyncAccounts(merchantInfos);
                        final var zenAccsFuture = zenAsyncService.zenDiffByUserForPbAccReconcile(appUser, startDate)
                                .thenApply(Optional::get)
                                .thenApply(zenCommonMapper::getZenAccounts);
                        return zenAccsFuture.thenCombine(pbAccsFuture, reconcileCommonMapper::mapInfoForAccountJson);
                    })
                    .thenApply(r -> new ResponseEntity<>(new PbZenReconcileResponse(r), OK))
                    .whenComplete((r, e) -> logReconcilePbJson(userId, st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot build pb/zen json for user: [{}], time: [{}], error [{}] - error", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(new ResponseEntity<>(NO_CONTENT));
        }
    }


    private CompletableFuture<AppUser> getUserAsync(String userId) {
        return awsUserService.getById(userId).thenApply(Optional::get);
    }
}
