package com.github.storytime.service.misc;

import com.github.storytime.config.props.Constants;
import com.github.storytime.model.api.SavingsInfo;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import static com.github.storytime.config.props.Constants.*;
import static org.apache.commons.lang3.StringUtils.*;

@Service
public class DigitsFormatter {

    public String formatAmount(final BigDecimal amount) {
        final String[] totalAsArray = amount.toPlainString().split(SPLITTER_EMPTY);
        final StreamEx<String> values = StreamEx.ofReversed(totalAsArray);
        final IntStreamEx indexes = IntStreamEx.range(START_INCLUSIVE, totalAsArray.length);

        final String formattedTotal = values.zipWith(indexes)
                .map(z -> z.getValue() % FORMATTER_SPLITTER == Constants.ZERO ? SPACE + z.getKey() : z.getKey())
                .collect(Collectors.joining());

        return StringUtils.reverse(formattedTotal.trim());
    }

    public StringBuilder mapToNiceSavingsString(final SavingsInfo si) {
        return new StringBuilder()
                .append(rightPad(si.getTitle() + DOTS + SPACE, SAVINGS_STRING_SIZE))
                .append(rightPad(formatAmount(si.getBalance()) + SPACE + si.getCurrencySymbol() + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                .append(rightPad(formatAmount(si.getInUah()) + SPACE + UAH + SLASH_SEPARATOR, SAVINGS_STRING_SIZE))
                .append(rightPad(si.getPercent() + SPACE + PERCENT, SAVINGS_PERCENT_SIZE))
                .append(LF);
    }
}
