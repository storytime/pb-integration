package com.github.storytime.service.sync;

import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.MerchantInfo;
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
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.allOf;
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
    public void sync(final Function<MerchantService, List<MerchantInfo>> selectMerchantsFk,
                     final UnaryOperator<List<List<Statement>>> filterAlreadyPushed,
                     final BiConsumer<List<List<Statement>>, List<MerchantInfo>> onSuccessFk,
                     final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFk,
                     final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFk) {

        final var st = createSt();
        userService.findAllAsync()
                .thenAccept(usersList -> usersList.forEach(user -> {
                            final var selectedMerchants = selectMerchantsFk.apply(merchantService);
                            final var futureList = selectedMerchants
                                    .stream()
                                    .map(m -> getListCompletableFuture(startDateFk, endDateFk, user, m)).toList();

                            // Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere
                            allOf(futureList.toArray(new CompletableFuture[selectedMerchants.size()]))
                                    .thenApply(v -> futureList.stream().map(CompletableFuture::join).toList())
                                    .thenApply(filterAlreadyPushed)
                                    .thenAccept(newPbTrList -> handleAll(newPbTrList, user, selectedMerchants, onSuccessFk, st));
                        })
                );
    }

    public void handleAll(final List<List<Statement>> newPbTrList,
                          final AppUser user,
                          final List<MerchantInfo> selectedMerchants,
                          final BiConsumer<List<List<Statement>>, List<MerchantInfo>> onSuccessFk,
                          final StopWatch st) {


        if (newPbTrList.isEmpty()) {
            LOGGER.debug("No new transaction for user: [{}], merch: [{}] time: [{}] - nothing to push - sync finished", user.getId(), selectedMerchants.size(), getTime(st));
            onSuccessFk.accept(emptyList(), selectedMerchants);
            return;
        }

        LOGGER.info("User: [{}], time: [{}], has: [{}] transactions to push", user.getId(), newPbTrList.size(), getTime(st));
        // step by step in one thread
        zenAsyncService.zenDiffByUserForPb(user)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbTrList, zenDiff, user))
                .thenApply(Optional::get)
                .thenCompose(tr -> zenAsyncService.pushToZen(user, tr))
                .thenApply(Optional::get)
                .thenCompose(zr -> userService.updateUserLastZenSyncTime(user.setZenLastSyncTimestamp(zr.getServerTimestamp())))
                .thenApply(Optional::get)
                .thenAccept(x -> onSuccessFk.accept(newPbTrList, selectedMerchants));
               // .thenAccept(v -> LOGGER.debug("User: [{}], time: [{}], transactions: [{}] were pushed. Sync completed!", user.getId(), newPbTrList.size(), getTime(st)));

    }


    private CompletableFuture<List<Statement>> getListCompletableFuture(final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFunction,
                                                                        final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFunction,
                                                                        final AppUser user,
                                                                        final MerchantInfo merch) {
        final var startDate = startDateFunction.apply(user, merch);
        final var endDate = endDateFunction.calculate(user, merch, startDate);
        return pbStatementsService.getPbTransactions(user, merch, startDate, endDate);
    }
}
