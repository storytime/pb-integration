package com.github.storytime.service.pb;

import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.pb.PbToZenDataMapper;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.async.SqsAsyncPublisherService;
import com.github.storytime.service.async.StatementAsyncService;
import com.github.storytime.service.async.UserAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
import com.github.storytime.service.misc.CustomPayeeService;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static com.github.storytime.error.AsyncErrorHandlerUtil.*;
import static com.github.storytime.mapper.PbStatementsAlreadyPushedUtil.generateUniqString;
import static com.github.storytime.service.util.STUtils.*;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);
    private final PbStatementsService pbStatementsService;
    private final UserAsyncService userAsyncService;
    private final PbToZenDataMapper pbToZenDataMapper;
    private final ZenAsyncService zenAsyncService;
    private final SqsAsyncPublisherService sqsAsyncPublisherService;
    private final StatementAsyncService statementAsyncService;
    private final CustomPayeeService customPayeeService;

    @Autowired
    public PbSyncService(
            final PbStatementsService pbStatementsService,
            final ZenAsyncService zenAsyncService,
            final PbToZenDataMapper pbToZenDataMapper,
            final UserAsyncService userAsyncService,
            final StatementAsyncService statementAsyncService,
            final CustomPayeeService customPayeeService,
            final SqsAsyncPublisherService sqsAsyncPublisherService) {
        this.zenAsyncService = zenAsyncService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenDataMapper = pbToZenDataMapper;
        this.sqsAsyncPublisherService = sqsAsyncPublisherService;
        this.userAsyncService = userAsyncService;
        this.statementAsyncService = statementAsyncService;
        this.customPayeeService = customPayeeService;
    }

    @Async
    public void sync(final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onSuccessFk,
                     final BiFunction<AppUser, PbMerchant, ZonedDateTime> startDateFk,
                     final TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> endDateFk) {

        final var st = createSt();
        final CompletableFuture<List<CompletableFuture<String>>> allUsersSyncCf =
                userAsyncService.getAllUsers()
                        .thenApply(awsUsers -> awsUsers.stream().filter(AppUser::isEnabled).map(user -> doSyncForAwsEachUser(onSuccessFk, startDateFk, endDateFk, user)).toList()); // //one for each user

        allUsersSyncCf.thenCompose(this::waitForAllCompleteAndPushToSqs)
                .thenAccept(x -> LOGGER.info("#################### All sync done time: [{}] ####################", getTimeAndReset(st)))
                .whenComplete((r, e) -> logAllSync(st, LOGGER, e));
    }

    // cheap, join for all started sync, threads thenApply(newPbTrList -> handleAwsAll(newPbTrList, awsUser, selectedMerchants, onSuccessFk, st))
    private CompletableFuture<String> waitForAllCompleteAndPushToSqs(final List<CompletableFuture<String>> allCfList) {
        return allOf(allCfList.toArray(new CompletableFuture[allCfList.size()]))
                .thenCompose(allDone -> sqsAsyncPublisherService.publishFinishMessage());
    }


    private CompletableFuture<String> doSyncForAwsEachUser(final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onSuccessFk,
                                                           final BiFunction<AppUser, PbMerchant, ZonedDateTime> startDateFk,
                                                           final TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> endDateFk,
                                                           final AppUser appUser) {

        final var st = createSt();
        final var selectedMerchants = appUser.getPbMerchant().stream().filter(PbMerchant::getEnabled).toList();
        final var pbCfList = selectedMerchants
                .stream()
                .map(m -> getAwsListOfPbCf(startDateFk, endDateFk, appUser, m)).toList();

        //* Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere */
        return allOf(pbCfList.toArray(new CompletableFuture[selectedMerchants.size()]))
                .thenApply(v -> pbCfList.stream().map(CompletableFuture::join).toList())
                .thenCompose(userLevelStatementsUnFiltered -> filterUserLevelStatements(appUser, userLevelStatementsUnFiltered))
                .thenCompose(userLevelStatementsFiltered -> handleAllForUser(userLevelStatementsFiltered, appUser, selectedMerchants, onSuccessFk, st))
                .whenComplete((r, e) -> logSyncInitPerUser(appUser.getId(), st, LOGGER, e));
    }

    private CompletableFuture<List<List<Statement>>> filterUserLevelStatements(final AppUser appUser, final List<List<Statement>> userLevelStatements) {
        return statementAsyncService
                .getAllStatementsByUser(appUser.getId())
                .thenApply(dbPbStatement -> userLevelStatements.stream()
                        .map(merchantLevel -> merchantLevel
                                .stream()
                                .filter(not(ap -> ofNullable(dbPbStatement.getAlreadyPushed()).orElse(emptySet()).contains(generateUniqString(ap))))
                                .toList())
                        .filter(not(List::isEmpty)).toList())
                .whenComplete((r, e) -> logSyncStatements(appUser.getId(), LOGGER, e));
    }


    public CompletableFuture<String> handleAllForUser(final List<List<Statement>> newPbTrList,
                                                      final AppUser user,
                                                      final List<PbMerchant> selectedMerchants,
                                                      final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onSuccessFk,
                                                      final StopWatch st) {

        var allTrList = newPbTrList.stream().flatMap(List::stream).toList();
        if (allTrList.isEmpty()) {
            return customPayeeService.getPayeeByUserId(user.getId())
                    .thenApply(newCustomerPayee -> customPayeeService.mergeUserPayees(newCustomerPayee, user))
                    .thenCompose(userAsyncService::updateUser)
                    .thenApply(Optional::get)
                    .thenApply(AppUser::getId)
                    .whenComplete((r, e) -> logSyncPushByUserEmpty(user.getId(), st, selectedMerchants.size(), LOGGER, e));
        }

        LOGGER.info("User: [{}] has: [{}] transactions to push, time: [{}]", user.getId(), allTrList.size(), getTime(st));

        // step by step in one thread
        return customPayeeService
                .getPayeeByUserId(user.getId())
                .thenApply(newCustomerPayee -> customPayeeService.mergeUserPayees(newCustomerPayee, user))
                .thenCompose(zenAsyncService::zenDiffByUserForPb)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> pbToZenDataMapper.buildZenReqFromPbData(newPbTrList, zenDiff, user))
                .thenApply(Optional::get)
                .thenCompose(tr -> zenAsyncService.pushToZen(user, tr))
                .thenApply(Optional::get)
                .thenCompose(zr -> userAsyncService.updateUser(user.setZenLastSyncTimestamp(zr.getServerTimestamp())))
                .thenApply(Optional::get)
                .thenApply(AppUser::getId)
                .thenCompose(x -> onSuccessFk.apply(newPbTrList, user.getId()))
                .thenApply(Optional::get)
                .thenApply(PbStatement::getUserId)
                .whenComplete((r, e) -> logSyncPushByUserNotEmpty(user.getId(), st, LOGGER, e));
    }


    private CompletableFuture<List<Statement>> getAwsListOfPbCf(final BiFunction<AppUser, PbMerchant, ZonedDateTime> startDateFunction,
                                                                final TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> endDateFunction,
                                                                final AppUser user,
                                                                final PbMerchant merch) {
        final var startDate = startDateFunction.apply(user, merch);
        final var endDate = endDateFunction.calculate(user, merch, startDate);
        return pbStatementsService.getAwsPbTransactions(user, merch, startDate, endDate);
    }

}
