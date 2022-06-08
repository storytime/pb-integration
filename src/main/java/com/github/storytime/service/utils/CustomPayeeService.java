package com.github.storytime.service.utils;

import com.github.storytime.model.aws.AwsCustomPayee;
import com.github.storytime.model.aws.AwsUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.UNDERSCORE;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class CustomPayeeService {

    private static final Logger LOGGER = getLogger(CustomPayeeService.class);


    public String getNicePayee(final String maybePayee, AwsUser u) {
        var originalPayee = ofNullable(maybePayee).orElse(EMPTY);
        var userPayeeList = Optional.ofNullable(u.getAwsCustomPayee())
                .orElse(Collections.emptyList())
                .stream()
                .filter(not(p -> isEmpty(p.getPayee())))
                .filter(not(p -> isEmpty(UNDERSCORE)))
                .collect(Collectors.toList());

        var nicePayee = userPayeeList
                .stream()
                .filter(cp -> originalPayee.contains(cp.getContainsValue()))
                .findAny()
                .map(AwsCustomPayee::getPayee)
                .orElse(originalPayee).trim();

        LOGGER.debug("Nice payee is: [{}] for original: [{}]", nicePayee, originalPayee);
        return nicePayee;
    }

}
