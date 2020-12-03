package com.github.storytime.service.access;


import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.internal.ExpiredPbStatement;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toSet;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PushedPbZenTransactionStorageService {

    private static final Logger LOGGER = getLogger(PushedPbZenTransactionStorageService.class);

    private final Set<ExpiredPbStatement> pushedPbZenTransactionStorage;
    private final CustomConfig customConfig;

    @Autowired
    public PushedPbZenTransactionStorageService(final Set<ExpiredPbStatement> pushedPbZenTransactionStorage,
                                                final CustomConfig customConfig) {
        this.pushedPbZenTransactionStorage = pushedPbZenTransactionStorage;
        this.customConfig = customConfig;
    }

    public void cleanOldPbToZenTransactionStorage() {
        LOGGER.debug("Going to clean already mapped PB storage");
        final long currentTime = now().toEpochMilli();
        final Set<ExpiredPbStatement> toDelete = pushedPbZenTransactionStorage
                .stream()
                .filter(eti -> currentTime - eti.getTransactionItemTime() > customConfig.getPushedPbZenTransactionStorageCleanOlderMillis())
                .collect(toSet());

        LOGGER.debug("Deleting: [{}] pushed PB from storage that contains: [{}]", toDelete.size(), pushedPbZenTransactionStorage.size());
        LOGGER.trace("Zen storage: [{}]", pushedPbZenTransactionStorage);
        pushedPbZenTransactionStorage.removeAll(toDelete);
        LOGGER.trace("Current size of PB storage: [{}]", pushedPbZenTransactionStorage);
    }
}
