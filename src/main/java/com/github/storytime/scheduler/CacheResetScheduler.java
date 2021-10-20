package com.github.storytime.scheduler;

import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.github.storytime.config.props.CacheNames.*;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CacheResetScheduler {

    private static final Logger LOGGER = getLogger(CacheResetScheduler.class);

    @Scheduled(fixedRateString = "${cache.clean.currency.millis}")
    @CacheEvict(allEntries = true, value = {CURRENCY_CACHE})
    public void cleaningCurrencyCache() {
        LOGGER.debug("Cleaning up currency cache ...");
    }

    @Scheduled(fixedRateString = "${cache.clean.zentags.millis}")
    @CacheEvict(allEntries = true, value = {TR_TAGS_DIFF})
    public void cleaningZenDiffTagsCache() {
        LOGGER.debug("Cleaning up tags cache ...");
    }

    @Scheduled(fixedRateString = "${cache.clean.payee.millis}")
    @CacheEvict(allEntries = true, value = {CUSTOM_PAYEE})
    public void cleaningCustomPayeeCache() {
        LOGGER.debug("Cleaning up custom payee cache ...");
    }
}
