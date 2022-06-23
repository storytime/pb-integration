package com.github.storytime.service.sync;

import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.PbStatementsToDynamoDbMapper;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.aws.AwsMerchant;
import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.PbStatementsService;
import com.github.storytime.service.async.SqsAsyncPublisherService;
import com.github.storytime.service.async.StatementAsyncService;
import com.github.storytime.service.async.UserAsyncService;
import com.github.storytime.service.async.ZenAsyncService;
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
import static com.github.storytime.service.utils.STUtils.*;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);
    private final PbStatementsService pbStatementsService;
    private final UserAsyncService userAsyncService;
    private final PbToZenMapper pbToZenMapper;
    private final ZenAsyncService zenAsyncService;
    private final SqsAsyncPublisherService sqsAsyncPublisherService;
    private final StatementAsyncService statementAsyncService;

    @Autowired
    public PbSyncService(
            final PbStatementsService pbStatementsService,
            final ZenAsyncService zenAsyncService,
            final PbToZenMapper pbToZenMapper,
            final UserAsyncService userAsyncService,
            final StatementAsyncService statementAsyncService,
            final SqsAsyncPublisherService sqsAsyncPublisherService) {
        this.zenAsyncService = zenAsyncService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
        this.sqsAsyncPublisherService = sqsAsyncPublisherService;
        this.userAsyncService = userAsyncService;
        this.statementAsyncService = statementAsyncService;
    }

    @Async
    public void sync(final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                     final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFk,
                     final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFk) {

        final var st = createSt();
        final CompletableFuture<List<CompletableFuture<String>>> allUsersSyncCf =
                userAsyncService.getAllUsers()
                        .thenApply(awsUsers -> awsUsers.stream().map(user -> doSyncForAwsEachUser(onSuccessFk, startDateFk, endDateFk, user)).toList()); // //one for each user

        allUsersSyncCf.thenCompose(this::waitForAllCompleteAndPushToSqs)
                .thenAccept(x -> LOGGER.info("#################### All sync done time: [{}] ####################", getTimeAndReset(st)))
                .whenComplete((r, e) -> logAllSync(st, LOGGER, e));
    }

    // cheap, join for all started sync, threads thenApply(newPbTrList -> handleAwsAll(newPbTrList, awsUser, selectedMerchants, onSuccessFk, st))
    private CompletableFuture<String> waitForAllCompleteAndPushToSqs(final List<CompletableFuture<String>> allCfList) {
        return allOf(allCfList.toArray(new CompletableFuture[allCfList.size()]))
                .thenCompose(allDone -> sqsAsyncPublisherService.publishFinishMessage());
    }


    private CompletableFuture<String> doSyncForAwsEachUser(final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                                                           final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFk,
                                                           final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFk,
                                                           final AwsUser awsUser) {

        final var st = createSt();
        final var selectedMerchants = awsUser.getAwsMerchant();
        final var pbCfList = selectedMerchants
                .stream()
                .map(m -> getAwsListOfPbCf(startDateFk, endDateFk, awsUser, m)).toList();

        //* Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere */
        return allOf(pbCfList.toArray(new CompletableFuture[selectedMerchants.size()]))
                .thenApply(v -> pbCfList.stream().map(CompletableFuture::join).toList())
                .thenCompose(userLevelStatementsUnFiltered -> filterUserLevelStatements(awsUser, userLevelStatementsUnFiltered))
                .thenCompose(userLevelStatementsFiltered -> handleAllForUser(userLevelStatementsFiltered, awsUser, selectedMerchants, onSuccessFk, st))
                .whenComplete((r, e) -> logSyncInitPerUser(awsUser.getId(), st, LOGGER, e));
    }

    private CompletableFuture<List<List<Statement>>> filterUserLevelStatements(final AwsUser awsUser, final List<List<Statement>> userLevelStatements) {
        return statementAsyncService
                .getAllStatementsByUser(awsUser.getId())
                .thenApply((AwsPbStatement dbData) -> userLevelStatements.stream().map(merchantLevel -> merchantLevel
                        .stream()
                        .filter(ap -> !dbData.getAlreadyPushed().contains(PbStatementsToDynamoDbMapper.generateUniqString(ap)))
                        .toList()).filter(not(List::isEmpty)).toList())
                .whenComplete((r, e) -> logSyncStatements(awsUser.getId(), LOGGER, e));
    }


    public CompletableFuture<String> handleAllForUser(final List<List<Statement>> newPbTrList,
                                                      final AwsUser user,
                                                      final List<AwsMerchant> selectedMerchants,
                                                      final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                                                      final StopWatch st) {

        var allTrList = newPbTrList.stream().flatMap(List::stream).toList();
        if (allTrList.isEmpty()) {
            return userAsyncService.updateUser(user)
                    .thenApply(Optional::get)
                    .thenApply(AwsUser::getId)
                    .whenComplete((r, e) -> logSyncPushByUserEmpty(user.getId(), st, selectedMerchants.size(), LOGGER, e));
        }

        LOGGER.info("User: [{}] has: [{}] transactions to push, time: [{}]", user.getId(), allTrList.size(), getTime(st));

        // step by step in one thread
        return zenAsyncService.zenDiffByUserForPb(user)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbTrList, zenDiff, user))
                .thenApply(Optional::get)
                .thenCompose(tr -> zenAsyncService.pushToZen(user, tr))
                .thenApply(Optional::get)
                .thenCompose(zr -> userAsyncService.updateUser(user.setZenLastSyncTimestamp(zr.getServerTimestamp())))
                .thenApply(Optional::get)
                .thenApply(AwsUser::getId)
                .thenCompose(x -> onSuccessFk.apply(newPbTrList, user.getId()))
                .thenApply(Optional::get)
                .thenApply(AwsPbStatement::getId)
                .whenComplete((r, e) -> logSyncPushByUserNotEmpty(user.getId(), st, LOGGER, e));
    }


    private CompletableFuture<List<Statement>> getAwsListOfPbCf(final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFunction,
                                                                final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFunction,
                                                                final AwsUser user,
                                                                final AwsMerchant merch) {
        final var startDate = startDateFunction.apply(user, merch);
        final var endDate = endDateFunction.calculate(user, merch, startDate);
        return pbStatementsService.getAwsPbTransactions(user, merch, startDate, endDate);
    }

}
