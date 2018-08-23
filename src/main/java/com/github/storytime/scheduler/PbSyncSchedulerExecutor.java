package com.github.storytime.scheduler;

import com.github.storytime.service.MerchantService;
import com.github.storytime.service.PbSyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.github.storytime.model.db.inner.SyncPriority.FIRST;
import static com.github.storytime.model.db.inner.SyncPriority.SECOND;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PbSyncSchedulerExecutor {

    private static final Logger LOGGER = getLogger(PbSyncSchedulerExecutor.class);
    private final PbSyncService pbSyncService;

    @Autowired
    public PbSyncSchedulerExecutor(final PbSyncService pbSyncService) {
        this.pbSyncService = pbSyncService;
    }

    @Scheduled(fixedRateString = "${sync.first.priority.period.millis}")
    public void firstPrioritySync() {
        LOGGER.debug("Starting first priority sync");
        pbSyncService.sync(merchantService -> merchantService.getAllEnabledMerchantsBySyncPriority(FIRST));
    }

    @Scheduled(fixedRateString = "${sync.second.priority.period.millis}", initialDelayString = "${sync.second.priority.delay.millis}")
    public void secondPrioritySync() {
        LOGGER.debug("Starting second priority sync");
        pbSyncService.sync(merchantService -> merchantService.getAllEnabledMerchantsBySyncPriority(SECOND));
    }

    @Scheduled(fixedRateString = "${sync.general.priority.period.millis}", initialDelayString = "${sync.general.priority.delay.millis}")
    public void generalPrioritySync() {
        LOGGER.debug("Starting general priority sync");
        pbSyncService.sync(MerchantService::getAllEnabledMerchantsWithPriority);
    }
}
