package com.github.storytime.service.misc;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.CustomPayee;
import com.github.storytime.service.async.UserAsyncService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.error.AsyncErrorHandlerUtil.logGetPayee;
import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Service
public class CustomPayeeService {

    private static final Logger LOGGER = LogManager.getLogger(CustomPayeeService.class);
    private final UserAsyncService userAsyncService;

    @Autowired
    public CustomPayeeService(final UserAsyncService userAsyncService) {
        this.userAsyncService = userAsyncService;
    }

    public CompletableFuture<List<CustomPayee>> getPayeeByUserId(final String userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling get payee for user: [{}] - start", userId);
            return userAsyncService.getById(userId)
                    .thenApply(Optional::get)
                    .thenApply(AppUser::getCustomPayee)
                    .whenComplete((r, e) -> logGetPayee(st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot get payee for user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }


    public CompletableFuture<List<CustomPayee>> updatePayeeByUserId(final String userId) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling update payee for user: [{}] - start", userId);
            return userAsyncService.getById(userId)
                    .thenApply(Optional::get)
                    .thenApply(AppUser::getCustomPayee)
                    .whenComplete((r, e) -> logGetPayee(st, LOGGER, e));
        } catch (Exception e) {
            LOGGER.error("Cannot get payee for user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(emptyList());
        }
    }
}
