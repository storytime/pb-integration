package com.github.storytime.service.misc;

import com.github.storytime.model.api.CustomPayee;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.service.async.UserAsyncService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.error.AsyncErrorHandlerUtil.logGetPayee;
import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.not;
import static java.util.stream.Stream.of;

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


    public CompletableFuture<ResponseEntity<Void>> updatePayeeByUserId(final String userId,
                                                                       final List<CustomPayee> toUpdateList) {
        final var st = createSt();
        try {
            LOGGER.debug("Calling update payees for user: [{}] - start", userId);
            return userAsyncService.getById(userId)
                    .thenApply(Optional::get)
                    .thenApply(appUser -> mergeUserPayees(toUpdateList, appUser))
                    .thenCompose(userAsyncService::updateUser)
                    .thenApply(updatedUser -> ResponseEntity.ok().<Void>build())
                    .whenComplete((r, e) -> logGetPayee(st, LOGGER, e));
        } catch (final Exception e) {
            LOGGER.error("Cannot update payees for user: [{}], time [{}], error: [{}] - error, endpoint ===", userId, getTimeAndReset(st), e.getCause(), e);
            return completedFuture(ResponseEntity.internalServerError().build());
        }
    }

    private AppUser mergeUserPayees(final List<CustomPayee> toUpdateList, final AppUser appUser) {
        final List<CustomPayee> userCustomPayee = appUser.getCustomPayee();
        final List<CustomPayee> newPayees = toUpdateList.stream().filter(not(userCustomPayee::contains)).toList();
        final List<CustomPayee> updatedPayees = toUpdateList.stream().filter(not(newPayees::contains)).toList();
        final List<CustomPayee> oldPayees = userCustomPayee.stream().filter(not(updatedPayees::contains)).toList();

        final List<CustomPayee> mergedList = of(newPayees, updatedPayees, oldPayees)
                .flatMap(Collection::stream)
                .toList();

        appUser.setCustomPayee(mergedList);
        return appUser;
    }
}
