package com.github.storytime.scheduler;

import com.github.storytime.function.PbSyncLambdaHolder;
import com.github.storytime.function.TrioFunction;
import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.aws.PbMerchant;
import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.async.StatementAsyncService;
import com.github.storytime.service.pb.PbSyncService;
import com.github.storytime.service.misc.DateService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncSchedulerExecutor {

    private static final Logger LOGGER = getLogger(PbSyncSchedulerExecutor.class);

    private final PbSyncService pbSyncService;
    private final BiFunction<List<List<Statement>>, String, CompletableFuture<Optional<PbStatement>>> onSuccessFk;
    private final BiFunction<AppUser, PbMerchant, ZonedDateTime> startDateFk;
    private final TrioFunction<AppUser, PbMerchant, ZonedDateTime, ZonedDateTime> endDateFk;

    @Autowired
    public PbSyncSchedulerExecutor(final PbSyncService pbSyncService,
                                   final DateService dateService,
                                   final StatementAsyncService statementAsyncService,
                                   final PbSyncLambdaHolder pbSyncLambdaHolder) {
        this.pbSyncService = pbSyncService;
        this.onSuccessFk = pbSyncLambdaHolder.onAwsDbRegularSyncSuccess(statementAsyncService);
        this.startDateFk = pbSyncLambdaHolder.getAwsStartDate(dateService);
        this.endDateFk = pbSyncLambdaHolder.getAwsEndDate();
    }


    @Scheduled(fixedRateString = "${sync.first.priority.period.millis}")
    public void awsSync() {
        LOGGER.debug("#################### Starting sync");
        pbSyncService.sync(onSuccessFk, startDateFk, endDateFk);
    }

}
