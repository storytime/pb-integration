package com.github.storytime.service.sync;

import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.PbStatementsService;
import com.github.storytime.service.ZenDiffService;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.access.UserService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.storytime.error.AsyncErrorHandlerUtil.getZenDiffUpdateHandler;
import static com.github.storytime.function.FunctionUtils.logAndGetEmptyForSync;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.apache.logging.log4j.Level.WARN;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);

    private final MerchantService merchantService;
    private final PbStatementsService pbStatementsService;
    private final UserService userService;
    private final PbToZenMapper pbToZenMapper;
    private final ZenDiffService zenDiffService;

    @Autowired
    public PbSyncService(final MerchantService merchantService,
                         final PbStatementsService pbStatementsService,
                         final UserService userService,
                         final ZenDiffService zenDiffService,
                         final PbToZenMapper pbToZenMapper) {
        this.merchantService = merchantService;
        this.userService = userService;
        this.zenDiffService = zenDiffService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
    }

    @Async
    public void sync(final Function<MerchantService, Optional<List<MerchantInfo>>> selectFunction,
                     final Function<List<List<Statement>>, List<ExpiredPbStatement>> pbTransactionMapper,
                     final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccessFunction,
                     final Consumer<List<MerchantInfo>> onEmptyFunction,
                     final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFunction,
                     final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFunction) {

        userService.findAll()
                .forEach(user -> selectFunction.apply(merchantService)
                        .map(merchantLists -> of(merchantLists
                                .stream()
                                .map(merch -> {
                                    final var startDate = startDateFunction.apply(user, merch);
                                    final var endDate = endDateFunction.calculate(user, merch, startDate);
                                    return pbStatementsService.getPbTransactions(user, merch, startDate, endDate);
                                }) // create async requests
                                .collect(toUnmodifiableList()))
                                .flatMap(cfList -> of(CompletableFuture.allOf(cfList.toArray(new CompletableFuture[merchantLists.size()])) // wait for completions of all requests
                                        .thenApply(aVoid -> cfList.stream().map(CompletableFuture::join).collect(toUnmodifiableList())) // collect results
                                        .thenAccept(newPbDataList -> handlePbCfRequestData(user, merchantLists, newPbDataList, pbTransactionMapper, onSuccessFunction, onEmptyFunction))))) // process all data
                        .or(logAndGetEmptyForSync(LOGGER, WARN, "No merchants to sync")));
    }

    public void handlePbCfRequestData(final AppUser appUser,
                                      final List<MerchantInfo> merchants,
                                      final List<List<Statement>> newPbDataList,
                                      final Function<List<List<Statement>>, List<ExpiredPbStatement>> pbTransactionMapper,
                                      final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccess,
                                      final Consumer<List<MerchantInfo>> onEmpty) {

        final var maybeToPush = pbTransactionMapper.apply(newPbDataList);

        if (maybeToPush.isEmpty()) {
            LOGGER.info("No new transaction for user:[{}] Nothing to push in current sync thread", appUser.getId());
            onEmpty.accept(merchants);
        } else {
            LOGGER.info("User:[{}] has:[{}] transactions sync period", appUser.getId(), newPbDataList.size());
            // step by step in one thread
            zenDiffService.zenDiffByUserForPb(appUser)
                    .thenApply(zenDiffResponse -> zenDiffResponse
                            .flatMap(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbDataList, zenDiff, appUser)))
                    .thenApply(zenDiffRequest -> zenDiffRequest
                            .flatMap(zr -> zenDiffService.pushToZen(appUser, zr)))
                    .thenApply(zenResponse -> zenResponse
                            .flatMap(zr -> userService.updateUserLastZenSyncTime(appUser.setZenLastSyncTimestamp(zr.getServerTimestamp()))))
                    .thenAccept(au -> au.ifPresent(saveUserInfo -> onSuccess.accept(maybeToPush, merchants)))
                    .handle(getZenDiffUpdateHandler());
        }
    }
}
