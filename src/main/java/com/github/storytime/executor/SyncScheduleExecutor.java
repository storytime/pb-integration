package com.github.storytime.executor;

import com.github.storytime.service.SyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class SyncScheduleExecutor {

    private static final Logger LOGGER = getLogger(SyncScheduleExecutor.class);
    private final SyncService syncService;

    @Autowired
    public SyncScheduleExecutor(final SyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
        LOGGER.debug("Starting scheduled sync");
        syncService.sync();
    }
}
