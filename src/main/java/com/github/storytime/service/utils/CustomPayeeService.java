package com.github.storytime.service.utils;

import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.repository.CustomPayeeRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.storytime.config.props.CacheNames.CUSTOM_PAYEE;
import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class CustomPayeeService {

    private static final Logger LOGGER = getLogger(CustomPayeeService.class);
    private final CustomPayeeRepository customPayeeRepository;

    @Autowired
    public CustomPayeeService(final CustomPayeeRepository customPayeeRepository) {
        this.customPayeeRepository = customPayeeRepository;
    }

    public String getNicePayee(final String maybePayee) {
        var originalPayee = ofNullable(maybePayee).orElse(EMPTY);
        var nicePayee = findAll()
                .stream()
                .filter(cp -> originalPayee.contains(cp.getContainsValue()))
                .findAny()
                .map(CustomPayee::getPayee)
                .orElse(originalPayee).trim();

        LOGGER.debug("Nice payee is: [{}] for original: [{}]", nicePayee, originalPayee);
        return nicePayee;
    }

    @Cacheable(CUSTOM_PAYEE)
    public List<CustomPayee> findAll() {
        return customPayeeRepository.findAll();
    }
}
