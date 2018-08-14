package com.github.storytime.service;

import com.github.storytime.model.db.CustomPayee;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class CustomPayeeService {

    private static final Logger LOGGER = getLogger(CustomPayeeService.class);

    private final Set<CustomPayee> customPayeeValues;

    @Autowired
    public CustomPayeeService(final Set<CustomPayee> customPayeeValues) {
        this.customPayeeValues = customPayeeValues;
    }

    public String getNicePayee(final String maybePayee) {
        final String originalPayee = ofNullable(maybePayee).orElse(EMPTY);
        final String customPayee = customPayeeValues
                .stream()
                .filter(cp -> originalPayee.contains(cp.getContainsValue()))
                .findAny()
                .map(CustomPayee::getPayee)
                .orElse(originalPayee).trim();
        LOGGER.debug("Found custom payee: '{}'", customPayee);
        return customPayee;
    }
}
