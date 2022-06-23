package com.github.storytime.mapper.response;

import com.github.storytime.model.aws.CurrencyRates;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.storytime.model.CurrencySource.PB_CASH;

@Component
public class CurrencyResponseMapper {

    public CurrencyRates mapPbCashCurrencyRates(final ZonedDateTime now,
                                                final String currencyType,
                                                final CashResponse response) {
        return buildRate(PB_CASH, now, currencyType, new BigDecimal(response.getBuy()), new BigDecimal(response.getSale()));
    }


    public CurrencyRates buildRate(final String cs,
                                   final ZonedDateTime date,
                                   final String currencyType,
                                   final BigDecimal sellPrate,
                                   final BigDecimal buyPrate) {
        return CurrencyRates.builder()
                .currencySource(cs)
                .currencyType(currencyType)
                .dateTime(date.toInstant().toEpochMilli())
                .buyRate(buyPrate)
                .sellRate(sellPrate)
                .build();
    }
}
