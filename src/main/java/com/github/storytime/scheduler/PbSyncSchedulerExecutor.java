package com.github.storytime.scheduler;

import com.github.storytime.function.PbSyncLambdaHolder;
import com.github.storytime.function.TrioFunction;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.storytime.model.db.inner.SyncPriority.FIRST;
import static com.github.storytime.model.db.inner.SyncPriority.SECOND;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncSchedulerExecutor {

    private static final Logger LOGGER = getLogger(PbSyncSchedulerExecutor.class);
    private final PbSyncService pbSyncService;
    private final Function<MerchantService, List<MerchantInfo>> selectFirstPrioMerchants;
    private final Function<MerchantService, List<MerchantInfo>> selectSecondPrioMerchants;
    private final Function<MerchantService, List<MerchantInfo>> selectGeneralPrioMerchants;
    private final Function<List<List<Statement>>, List<ExpiredPbStatement>> regularSyncMapper;
    private final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccessFk;
    private final Consumer<List<MerchantInfo>> onEmptyFk;
    private final BiFunction<AppUser, MerchantInfo, ZonedDateTime> startDateFk;
    private final TrioFunction<AppUser, MerchantInfo, ZonedDateTime, ZonedDateTime> endDateFk;

    @Autowired
    public PbSyncSchedulerExecutor(final PbSyncService pbSyncService,
                                   final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction,
                                   final MerchantService merchantService,
                                   final DateService dateService,
                                   final PbSyncLambdaHolder pbSyncLambdaHolder) {
        this.pbSyncService = pbSyncService;
        this.selectFirstPrioMerchants = ms -> ms.getAllEnabledMerchantsBySyncPriority(FIRST);
        this.selectSecondPrioMerchants = ms -> ms.getAllEnabledMerchantsBySyncPriority(SECOND);
        this.selectGeneralPrioMerchants = MerchantService::getAllEnabledMerchantsWithPriority;
        this.regularSyncMapper = pbSyncLambdaHolder.getRegularSyncMapper(pbSyncLambdaHolder.getRegularSyncPredicate(), alreadyMappedPbZenTransaction);
        this.onSuccessFk = pbSyncLambdaHolder.onRegularSyncSuccess(merchantService, alreadyMappedPbZenTransaction);
        this.onEmptyFk = pbSyncLambdaHolder.onEmptyFilter(merchantService);
        this.startDateFk = pbSyncLambdaHolder.getStartDate(dateService);
        this.endDateFk = pbSyncLambdaHolder.getEndDate();
    }

    @Scheduled(fixedRateString = "${sync.first.priority.period.millis}")
    public void firstPrioritySync() {
        LOGGER.debug("Starting first priority sync");
        pbSyncService.sync(selectFirstPrioMerchants, regularSyncMapper, onSuccessFk, onEmptyFk, startDateFk, endDateFk);
    }

    @Scheduled(fixedRateString = "${sync.second.priority.period.millis}", initialDelayString = "${sync.second.priority.delay.millis}")
    public void secondPrioritySync() {
        LOGGER.debug("Starting second priority sync");
        pbSyncService.sync(selectSecondPrioMerchants, regularSyncMapper, onSuccessFk, onEmptyFk, startDateFk, endDateFk);
    }

    @Scheduled(fixedRateString = "${sync.general.priority.period.millis}", initialDelayString = "${sync.general.priority.delay.millis}")
    public void generalPrioritySync() {
        LOGGER.debug("Starting general priority sync");
        pbSyncService.sync(selectGeneralPrioMerchants, regularSyncMapper, onSuccessFk, onEmptyFk, startDateFk, endDateFk);
    }
}
