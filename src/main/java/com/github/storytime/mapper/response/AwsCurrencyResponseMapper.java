package com.github.storytime.mapper.response;

import com.github.storytime.model.aws.AwsCurrencyRates;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component
public class AwsCurrencyResponseMapper {

    public AwsCurrencyRates mapPbCashCurrencyRates(final ZonedDateTime now,
                                                   final String currencyType,
                                                   final CashResponse response) {
        return buildRate("PB_CASH", now, currencyType, new BigDecimal(response.getBuy()), new BigDecimal(response.getSale()));
    }


    public AwsCurrencyRates buildRate(final String cs,
                                      final ZonedDateTime date,
                                      final String currencyType,
                                      final BigDecimal sellPrate,
                                      final BigDecimal buyPrate) {
        return new AwsCurrencyRates().builder()
                .currencySource(cs)
                .currencyType(currencyType)
                .dateTime(date.toInstant().toEpochMilli())
                .buyRate(buyPrate)
                .sellRate(sellPrate)
                .build();
    }
}
