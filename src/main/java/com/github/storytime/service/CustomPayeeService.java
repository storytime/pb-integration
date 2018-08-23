package com.github.storytime.service;

import com.github.storytime.model.db.CustomPayee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.ofNullable;

@Service
public class CustomPayeeService {

    private final Set<CustomPayee> customPayeeValues;

    @Autowired
    public CustomPayeeService(final Set<CustomPayee> customPayeeValues) {
        this.customPayeeValues = customPayeeValues;
    }

    public String getNicePayee(final String maybePayee) {
        final String originalPayee = ofNullable(maybePayee).orElse(EMPTY);
        return customPayeeValues
                .stream()
                .filter(cp -> originalPayee.contains(cp.getContainsValue()))
                .findAny()
                .map(CustomPayee::getPayee)
                .orElse(originalPayee).trim();
    }
}
