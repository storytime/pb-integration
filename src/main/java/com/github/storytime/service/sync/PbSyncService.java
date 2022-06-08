package com.github.storytime.service.sync;

import com.github.storytime.function.TrioFunction;
import com.github.storytime.mapper.PbToZenMapper;
import com.github.storytime.model.aws.AwsMerchant;
import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.AwsSqsPublisherService;
import com.github.storytime.service.AwsStatementService;
import com.github.storytime.service.AwsUserAsyncService;
import com.github.storytime.service.PbStatementsService;
import com.github.storytime.service.async.ZenAsyncService;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import static com.github.storytime.STUtils.*;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logSync;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.function.Predicate.not;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class PbSyncService {

    private static final Logger LOGGER = getLogger(PbSyncService.class);

    // private final MerchantService merchantService;
    private final PbStatementsService pbStatementsService;
    private final AwsUserAsyncService awsUserAsyncService;
    private final PbToZenMapper pbToZenMapper;
    private final ZenAsyncService zenAsyncService;
    private final AwsSqsPublisherService awsSqsPublisherService;
    private final AwsStatementService awsStatementService;


    @Autowired
    public PbSyncService(
            final PbStatementsService pbStatementsService,
            final ZenAsyncService zenAsyncService,
            final PbToZenMapper pbToZenMapper,
            final AwsUserAsyncService awsUserAsyncService,
            final AwsStatementService awsStatementService,
            final AwsSqsPublisherService awsSqsPublisherService) {
        this.zenAsyncService = zenAsyncService;
        this.pbStatementsService = pbStatementsService;
        this.pbToZenMapper = pbToZenMapper;
        this.awsSqsPublisherService = awsSqsPublisherService;
        this.awsUserAsyncService = awsUserAsyncService;
        this.awsStatementService = awsStatementService;
    }

    @Async
    public void sync(final UnaryOperator<List<List<Statement>>> filterAlreadyPushed,
                     final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                     final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFk,
                     final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFk) {


        final var st = createSt();

        final CompletableFuture<List<CompletableFuture<CompletableFuture<String>>>> allUsersSyncCf = awsUserAsyncService.getAllUsers()
                .thenApply(awsUserList -> awsUserList.stream().map(user -> {   //one for each user
                    return doSyncForAwsEachUser(filterAlreadyPushed, onSuccessFk, startDateFk, endDateFk, user);
                }).toList());

        allUsersSyncCf.thenAccept(allCfList -> {
            // cheap, join for all started sync, threads thenApply(newPbTrList -> handleAwsAll(newPbTrList, awsUser, selectedMerchants, onSuccessFk, st))
            List<CompletableFuture<String>> allUsersMerchantLevelCf = allCfList.stream().map(CompletableFuture::join).toList();
            CompletableFuture<Void> syncDoneCf = allOf(allUsersMerchantLevelCf.toArray(new CompletableFuture[allUsersMerchantLevelCf.size()]));
            syncDoneCf.thenAccept(allDone -> {
                awsSqsPublisherService.publishFinishMessage();
                LOGGER.info("#################### All sync done time: [{}] ####################", getTimeAndReset(st));
            });
        });
    }

    private CompletableFuture<CompletableFuture<String>> doSyncForAwsEachUser(final UnaryOperator<List<List<Statement>>> filterAlreadyPushed,
                                                                              final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                                                                              final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFk,
                                                                              final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFk,
                                                                              final AwsUser awsUser) {

        final var st = createSt();
        final var selectedMerchants = awsUser.getAwsMerchant();
        final var pbCfList = selectedMerchants
                .stream()
                .map(m -> getAwsListOfPbCf(startDateFk, endDateFk, awsUser, m)).toList();

        //* Since we’re calling future.join() when all the futures are complete, we’re not blocking anywhere */
        CompletableFuture<CompletableFuture<String>> completableFutureCompletableFuture = allOf(pbCfList.toArray(new CompletableFuture[selectedMerchants.size()]))
                .thenApply(v -> pbCfList.stream().map(CompletableFuture::join).toList())
                .thenCompose((List<List<Statement>> userLevel) -> {
                    CompletableFuture<List<List<Statement>>> listCompletableFuture = awsStatementService
                            .getAllStatementsByUser(awsUser.getId())
                            .thenApply((AwsPbStatement dny) -> {
                                Set<String> alreadyPushed = dny.getAlreadyPushed();
                                List<List<Statement>> lists = userLevel.stream().map((List<Statement> merchantLevel) -> merchantLevel
                                        .stream()
                                        .filter(ap -> !alreadyPushed.contains(AwsStatementService.generateUniqString(ap)))
                                        .toList()).filter(not(List::isEmpty)).toList();
                                return lists;
                            });

                    return listCompletableFuture;
                })
                .thenApply((List<List<Statement>> newPbTrList) -> handleAwsAll(newPbTrList, awsUser, selectedMerchants, onSuccessFk, st));

        return completableFutureCompletableFuture;
    }


    public CompletableFuture<String> handleAwsAll(final List<List<Statement>> newPbTrList,
                                                  final AwsUser user,
                                                  final List<AwsMerchant> selectedMerchants,
                                                  final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk,
                                                  StopWatch st) {

        var allTrList = newPbTrList.stream().flatMap(List::stream).toList();
        if (allTrList.isEmpty()) {
            LOGGER.debug("No new transaction for user: [{}], merch: [{}] time: [{}] - nothing to push - sync finished", user.getId(), selectedMerchants.size(), getTime(st));
            return awsUserAsyncService.updateUser(user)
                    .thenApply(Optional::get)
                    .thenApply(AwsUser::getId);
        }

        LOGGER.info("User: [{}] has: [{}] transactions to push, time: [{}]", user.getId(), allTrList.size(), getTime(st));

        // step by step in one thread

        CompletableFuture<String> objectCompletableFuture = zenAsyncService.zenDiffByUserForPb(user)
                .thenApply(Optional::get)
                .thenApply(zenDiff -> pbToZenMapper.buildZenReqFromPbData(newPbTrList, zenDiff, user))
                .thenApply(Optional::get)
                .thenCompose(tr -> zenAsyncService.pushToZen(user, tr))
                .thenApply(Optional::get)
                .thenCompose(zr -> awsUserAsyncService.updateUser(user.setZenLastSyncTimestamp(zr.getServerTimestamp())))
                .thenApply(Optional::get)
                .thenApply(AwsUser::getId)
                .thenCompose(x -> onSuccessFk.apply(newPbTrList, user.getId()))
                .thenApply(Optional::get)
                .thenApply(AwsPbStatement::getId)
                .whenComplete((r, e) -> logSync(user.getId(), st, LOGGER, e));

        return objectCompletableFuture;
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
