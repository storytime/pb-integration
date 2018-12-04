package com.github.storytime.service;

import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.repository.CustomPayeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.github.storytime.config.props.Constants.EMPTY;
import static java.util.Optional.ofNullable;

@Service
public class CustomPayeeService {


    private final CustomPayeeRepository customPayeeRepository;

    @Autowired
    public CustomPayeeService(CustomPayeeRepository customPayeeRepository) {
        this.customPayeeRepository = customPayeeRepository;
    }


    public String getNicePayee(final String maybePayee) {
        final String originalPayee = ofNullable(maybePayee).orElse(EMPTY).trim();
        return customPayeeRepository.findAll()
                .stream()
                .filter(cp -> originalPayee.contains(ofNullable(cp.getContainsValue()).orElse(EMPTY)))
                .findAny()
                .map(CustomPayee::getPayee)
                .orElse(originalPayee);
    }
}
