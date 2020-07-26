package com.github.storytime.mapper.response;

import com.github.storytime.model.currency.pb.cash.CashResponse;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;

@Component
public class CurrencyResponseMapper {

    public CurrencyRates mapPbCashCurrencyRates(final ZonedDateTime now,
                                                final CurrencyType currencyType,
                                                final CashResponse response) {
        return buildRate(PB_CASH, now, currencyType, new BigDecimal(response.getBuy()), new BigDecimal(response.getSale()));
    }


    public CurrencyRates buildRate(final CurrencySource cs,
                                   final ZonedDateTime date,
                                   final CurrencyType currencyType,
                                   final BigDecimal sellPrate,
                                   final BigDecimal buyPrate) {
        return new CurrencyRates()
                .setCurrencySource(cs)
                .setCurrencyType(currencyType)
                .setDate(date.toInstant().toEpochMilli())
                .setBuyRate(buyPrate)
                .setSellRate(sellPrate);
    }
}
