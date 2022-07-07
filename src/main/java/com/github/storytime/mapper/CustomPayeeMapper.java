package com.github.storytime.mapper;

import com.github.storytime.model.api.CustomPayee;
import com.github.storytime.model.aws.AppUser;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.github.storytime.config.props.Constants.EMPTY;
import static com.github.storytime.config.props.Constants.UNDERSCORE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class CustomPayeeMapper {

    public String getNicePayee(final String maybePayee, final AppUser u) {
        var originalPayee = ofNullable(maybePayee).orElse(EMPTY);
        final List<CustomPayee> userPayeeList = getCustomPayeesByUserIdForMapping(u);
        return userPayeeList
                .stream()
                .filter(cp -> originalPayee.contains(cp.getContainsValue()))
                .findAny()
                .map(CustomPayee::getPayee)
                .orElse(originalPayee).trim();
    }

    public List<CustomPayee> getCustomPayeesByUserIdForMapping(final AppUser u) {
        return ofNullable(u.getCustomPayee())
                .orElse(emptyList())
                .stream()
                .filter(not(p -> p.getPayee().equals(UNDERSCORE)))
                .filter(not(p -> isEmpty(p.getPayee())))
                .toList();
    }
}
