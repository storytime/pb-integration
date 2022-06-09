package com.github.storytime.scheduler;

import com.github.storytime.function.PbSyncLambdaHolder;
import com.github.storytime.function.TrioFunction;
import com.github.storytime.model.aws.AwsMerchant;
import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.AwsStatementService;
import com.github.storytime.service.sync.PbSyncService;
import com.github.storytime.service.utils.DateService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncSchedulerExecutor {

    private static final Logger LOGGER = getLogger(PbSyncSchedulerExecutor.class);

    private final PbSyncService pbSyncService;
    private final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<AwsPbStatement>>> onSuccessFk;
    private final BiFunction<AwsUser, AwsMerchant, ZonedDateTime> startDateFk;
    private final TrioFunction<AwsUser, AwsMerchant, ZonedDateTime, ZonedDateTime> endDateFk;

    @Autowired
    public PbSyncSchedulerExecutor(final PbSyncService pbSyncService,
                                   final Set<Statement> alreadyMappedPbZenTransaction,
                                   final DateService dateService,
                                   final AwsStatementService awsStatementService,
                                   final PbSyncLambdaHolder pbSyncLambdaHolder) {
        this.pbSyncService = pbSyncService;
        this.onSuccessFk = pbSyncLambdaHolder.onAwsDbRegularSyncSuccess(awsStatementService);
        this.startDateFk = pbSyncLambdaHolder.getAwsStartDate(dateService);
        this.endDateFk = pbSyncLambdaHolder.getAwsEndDate();
    }


    @Scheduled(fixedRateString = "${sync.first.priority.period.millis}")
    public void awsSync() {
        LOGGER.debug("#################### Starting sync");
        pbSyncService.sync(onSuccessFk, startDateFk, endDateFk);
    }

}
