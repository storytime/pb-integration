package com.github.storytime.executor;

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
public class SyncScheduleExecutor {

    private static final Logger LOGGER = getLogger(SyncScheduleExecutor.class);
    private final PbSyncService pbSyncService;

    @Autowired
    public SyncScheduleExecutor(final PbSyncService pbSyncService) {
        this.pbSyncService = pbSyncService;
    }

    @Scheduled(fixedRateString = "${sync.first.priority.period}")
    public void firstPrioritySync() {
        LOGGER.debug("Starting first priority sync");
        pbSyncService.sync(merchantService -> merchantService.getAllEnabledMerchantsBySyncPriority(FIRST));
    }

    @Scheduled(fixedRateString = "${sync.second.priority.period}", initialDelayString = "${sync.second.priority.delay}")
    public void secondPrioritySync() {
        LOGGER.debug("Starting second priority sync");
        pbSyncService.sync(merchantService -> merchantService.getAllEnabledMerchantsBySyncPriority(SECOND));
    }

    @Scheduled(fixedRateString = "${sync.general.priority.period}", initialDelayString = "${sync.general.priority.delay}")
    public void generalPrioritySync() {
        LOGGER.debug("Starting general priority sync");
        pbSyncService.sync(MerchantService::getAllEnabledMerchantsWithPriority);
    }
}
