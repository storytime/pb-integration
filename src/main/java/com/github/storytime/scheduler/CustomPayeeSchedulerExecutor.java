package com.github.storytime.scheduler;

import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.repository.CustomPayeeRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CustomPayeeSchedulerExecutor {

    private static final Logger LOGGER = getLogger(CustomPayeeSchedulerExecutor.class);
    private final CustomPayeeRepository customPayeeRepository;
    private final Set<CustomPayee> customPayeeValues;

    @Autowired
    public CustomPayeeSchedulerExecutor(final CustomPayeeRepository customPayeeRepository,
                                        final Set<CustomPayee> customPayeeValues) {
        this.customPayeeRepository = customPayeeRepository;
        this.customPayeeValues = customPayeeValues;
    }

    @Scheduled(fixedRateString = "${refresh.custom.payee.period.millis}", initialDelayString = "${refresh.custom.payee.delay.millis}")
    public void refreshCustomPayeeValue() {
        LOGGER.trace("Updating custom payee values from DB, count: [{}] - start", customPayeeValues.size());
        customPayeeValues.clear();
        customPayeeValues.addAll(customPayeeRepository.findAll());
        LOGGER.trace("Updated custom payee values from DB, new count: [{}] - finish", customPayeeValues.size());
    }
}
