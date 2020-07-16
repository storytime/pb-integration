package com.github.storytime.mapper.response;

import com.github.storytime.model.currency.pb.archive.ExchangeRateItem;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;
import com.github.storytime.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.CurrencySource.NBU;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.math.BigDecimal.valueOf;
import static java.util.Optional.ofNullable;

@Component
public class CurrencyResponseMapper {

    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyResponseMapper(final CurrencyRepository currencyRepository) {

        this.currencyRepository = currencyRepository;
    }

    public Optional<CurrencyRates> mapPbCashCurrencyRates(final ZonedDateTime now,
                                                          final CurrencyType currencyType,
                                                          final List<CashResponse> response) {
        //todo move save to service level
        return response.stream()
                .filter(cr -> ofNullable(cr.getBaseCcy()).orElse(EMPTY).equalsIgnoreCase(UAH_STR) &&
                        ofNullable(cr.getCcy()).orElse(EMPTY).equalsIgnoreCase(currencyType.toString()))
                .findFirst()
                .map(cr -> buildRate(PB_CASH, now, currencyType, new BigDecimal(cr.getBuy()), new BigDecimal(cr.getSale())))
                .map(currencyRepository::save);
    }

    public Optional<CurrencyRates> mapNbuCurrencyRates(final ZonedDateTime lastDay, final List<ExchangeRateItem> rates) {
        return rates.stream()
                .filter(cr ->
                        ofNullable(cr.getBaseCurrency()).orElse(EMPTY).equalsIgnoreCase(UAH_STR) &&
                                ofNullable(cr.getCurrency()).orElse(EMPTY).equalsIgnoreCase(USD_STR))
                .findFirst()
                .map(ExchangeRateItem::getPurchaseRateNB)
                .map(rate -> buildRate(NBU, lastDay, USD, valueOf(rate), valueOf(rate)))
                .map(currencyRepository::save);
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
