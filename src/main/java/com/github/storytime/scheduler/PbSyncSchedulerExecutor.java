package com.github.storytime.scheduler;

import com.github.storytime.function.PbSyncLambdaHolder;
import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.internal.ExpiredPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.service.access.MerchantService;
import com.github.storytime.service.sync.PbSyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.storytime.model.db.inner.SyncPriority.FIRST;
import static com.github.storytime.model.db.inner.SyncPriority.SECOND;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncSchedulerExecutor {

    private static final Logger LOGGER = getLogger(PbSyncSchedulerExecutor.class);
    private final PbSyncService pbSyncService;
    private final Function<MerchantService, Optional<List<MerchantInfo>>> selectFirstPrioMerchants;
    private final Function<MerchantService, Optional<List<MerchantInfo>>> selectSecondPrioMerchants;
    private final Function<MerchantService, Optional<List<MerchantInfo>>> selectGeneralPrioMerchants;
    private final Function<List<List<Statement>>, List<ExpiredPbStatement>> regularSyncMapper;
    private final BiConsumer<List<ExpiredPbStatement>, List<MerchantInfo>> onSuccess;
    private final Consumer<List<MerchantInfo>> onEmpty;

    @Autowired
    public PbSyncSchedulerExecutor(final PbSyncService pbSyncService,
                                   final Set<ExpiredPbStatement> alreadyMappedPbZenTransaction,
                                   final MerchantService merchantService,
                                   final PbSyncLambdaHolder pbSyncLambdaHolder) {
        this.pbSyncService = pbSyncService;
        this.selectFirstPrioMerchants = ms -> ms.getAllEnabledMerchantsBySyncPriority(FIRST);
        this.selectSecondPrioMerchants = ms -> ms.getAllEnabledMerchantsBySyncPriority(SECOND);
        this.selectGeneralPrioMerchants = MerchantService::getAllEnabledMerchantsWithPriority;
        this.regularSyncMapper = pbSyncLambdaHolder.getRegularSyncMapper(pbSyncLambdaHolder.getRegularSyncPredicate(), alreadyMappedPbZenTransaction);
        this.onSuccess = pbSyncLambdaHolder.onRegularSyncSuccess(merchantService, alreadyMappedPbZenTransaction);
        this.onEmpty = pbSyncLambdaHolder.onEmptyFilter(merchantService);
    }

    @Scheduled(fixedRateString = "${sync.first.priority.period.millis}")
    public void firstPrioritySync() {
        LOGGER.debug("Starting first priority sync");
        pbSyncService.sync(selectFirstPrioMerchants, regularSyncMapper, onSuccess, onEmpty);
    }

    @Scheduled(fixedRateString = "${sync.second.priority.period.millis}", initialDelayString = "${sync.second.priority.delay.millis}")
    public void secondPrioritySync() {
        LOGGER.debug("Starting second priority sync");
        pbSyncService.sync(selectSecondPrioMerchants, regularSyncMapper, onSuccess, onEmpty);
    }

    @Scheduled(fixedRateString = "${sync.general.priority.period.millis}", initialDelayString = "${sync.general.priority.delay.millis}")
    public void generalPrioritySync() {
        LOGGER.debug("Starting general priority sync");
        pbSyncService.sync(selectGeneralPrioMerchants, regularSyncMapper, onSuccess, onEmpty);
    }
}
