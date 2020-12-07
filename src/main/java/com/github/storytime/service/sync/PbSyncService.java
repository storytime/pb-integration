package com.github.storytime.service.sync;

import com.github.storytime.STUtils;
import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.PbStatementsService;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.error.AsyncErrorHandlerUtil.getZenDiffUpdateHandler;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);

    private final MerchantService merchantService;
    private final PbStatementsService pbStatementsService;
    private final UserService userService;
    private final PbToZenMapper pbToZenMapper;
    private final ZenAsyncService zenAsyncService;

    @Autowired
    public PbSyncService(final MerchantService merchantService,
                         final PbStatementsService pbStatementsService,
                         final UserService userService,
                         final ZenAsyncService zenAsyncService,
                         final PbToZenMapper pbToZenMapper) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.zenAsyncService = zenAsyncService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
    }

    @Async
    public void sync(final Function<MerchantService, List<MerchantInfo>> selectFunction,
                     final Function<List<List<Statement>>, List<ExpiredPbStatement>> pbTransactionMapper,
                     final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccessFunction,
                     final Consumer<List<MerchantInfo>> onEmptyFunction,
                     final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFunction,
                     final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFunction) {

        final var st = STUtils.createSt();
        userService.findAllAsync()
                .thenAccept(usersList -> usersList.forEach(user -> {
                            final List<MerchantInfo> selectedMerch = selectFunction.apply(merchantService);
                            final List<CompletableFuture<List<Statement>>> futureList = selectedMerch
                                    .stream()
                                    .map(merch -> getListCompletableFuture(startDateFunction, endDateFunction, user, merch)) // create async requests
                                    .collect(toUnmodifiableList());

                            // Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere
                            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[selectedMerch.size()]))
                                    .thenApply(aVoid -> futureList.stream().map(CompletableFuture::join).collect(toUnmodifiableList()))
                                    .thenAccept(newPbDataList -> handlePbCfRequestData(user, selectedMerch, newPbDataList, pbTransactionMapper, onSuccessFunction, onEmptyFunction, st));
                        })
                );
    }

    private CompletableFuture<List<Statement>> getListCompletableFuture(final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFunction,
                                                                        final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFunction,
                                                                        final AppUser user,
                                                                        final MerchantInfo merch) {
        final var startDate = startDateFunction.apply(user, merch);
        final var endDate = endDateFunction.calculate(user, merch, startDate);
        return pbStatementsService.getPbTransactions(user, merch, startDate, endDate);
    }

    public void handlePbCfRequestData(final AppUser appUser,
                                      final List<MerchantInfo> merchants,
                                      final List<List<Statement>> newPbDataList,
                                      final Function<List<List<Statement>>, List<ExpiredPbStatement>> pbTransactionMapper,
                                      final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccess,
                                      final Consumer<List<MerchantInfo>> onEmpty,
                                      final StopWatch st) {

        final var maybeToPush = pbTransactionMapper.apply(newPbDataList);

        if (maybeToPush.isEmpty()) {
            LOGGER.info("No new transaction for user: [{}], time: [{}] - nothing to push", appUser.getId(), getTime(st));
            onEmpty.accept(merchants);
        } else {
            LOGGER.info("User: [{}], time: [{}], has: [{}] transactions to sync", appUser.getId(), newPbDataList.size(), getTime(st));
            // step by step in one thread
            zenAsyncService.zenDiffByUserForPb(appUser)
                    .thenApply(Optional::get)
                    .thenApply(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbDataList, zenDiff, appUser))
                    .thenApply(Optional::get)
                    .thenCompose(tr -> zenAsyncService.pushToZen(appUser, tr))
                    .thenApply(Optional::get)
                    .thenCompose(zr -> userService.updateUserLastZenSyncTime(appUser.setZenLastSyncTimestamp(zr.getServerTimestamp())))
                    .thenApply(Optional::get)
                    .thenAccept(user -> onSuccess.accept(maybeToPush, merchants))
                    .handle(getZenDiffUpdateHandler());
        }
    }
}
