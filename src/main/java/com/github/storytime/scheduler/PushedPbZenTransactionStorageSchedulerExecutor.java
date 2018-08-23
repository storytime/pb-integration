package com.github.storytime.scheduler;

import com.github.storytime.service.PushedPbZenTransactionStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PushedPbZenTransactionStorageSchedulerExecutor {

    final PushedPbZenTransactionStorageService pushedPbZenTransactionStorageService;

    @Autowired
    public PushedPbZenTransactionStorageSchedulerExecutor(final PushedPbZenTransactionStorageService pushedPbZenTransactionStorageService) {
        this.pushedPbZenTransactionStorageService = pushedPbZenTransactionStorageService;
    }

    @Scheduled(fixedRateString = "${pushed.pb.zen.transaction.storage.clean.period.millis}",
            initialDelayString = "${pushed.pb.zen.transaction.storage.clean.delay.millis}")
    public void cleanUpStorage() {
        pushedPbZenTransactionStorageService.cleanOldPbToZenTransactionStorage();
    }

}
