package com.github.storytime.mapper;

import com.github.storytime.model.zen.InstrumentItem;
import com.github.storytime.model.zen.ZenResponse;
import org.springframework.stereotype.Component;

import static com.github.storytime.config.props.Constants.DEFAULT_CURRENCY_ZEN;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.substringAfter;

@Component
public class ZenInstrumentsMapper {

    public Integer getZenCurrencyFromPbTransaction(final ZenResponse zenDiff, final String transactionString) {
        final String shortName = substringAfter(transactionString, SPACE);
        return ofNullable(zenDiff.getInstrument()).orElse(emptyList())
                .stream()
                .filter(zenCurr -> zenCurr.getShortTitle().equalsIgnoreCase(shortName))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }

    public String getZenCurrencySymbol(final ZenResponse zenDiff, final int id) {
        return ofNullable(zenDiff.getInstrument()).orElse(emptyList())
                .stream()
                .filter(zenCurr -> zenCurr.getId() == id)
                .findFirst()
                .map(InstrumentItem::getSymbol)
                .orElseThrow(() -> new RuntimeException("Cannot get currency symbol"));
    }

}
