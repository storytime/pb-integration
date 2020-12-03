package com.github.storytime.service;

import com.github.storytime.mapper.response.CurrencyResponseMapper;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencyType;
import com.github.storytime.repository.CurrencyRepository;
import com.github.storytime.service.async.CurrencyAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalTime.MIN;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CurrencyService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);
    private final CurrencyRepository currencyRepository;
    private final CurrencyResponseMapper currencyResponseMapper;
    private final CurrencyAsyncService currencyAsyncService;

    @Autowired
    public CurrencyService(final CurrencyResponseMapper currencyResponseMapper,
                           final CurrencyAsyncService currencyAsyncService,
                           final CurrencyRepository currencyRepository) {

        this.currencyRepository = currencyRepository;
        this.currencyResponseMapper = currencyResponseMapper;
        this.currencyAsyncService = currencyAsyncService;
    }

    public BigDecimal convertDivide(final Double from, final Double to) {
        final var cardSum = valueOf(abs(from));
        final var operationSum = valueOf(to);
        return cardSum.divide(operationSum, HALF_UP).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    public BigDecimal convertDivide(final Float from, final BigDecimal rate) {
        final var cardSum = valueOf(abs(from));
        return cardSum.divide(rate, CURRENCY_SCALE, HALF_UP).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    public Optional<CurrencyRates> pbUsdCashDayRates(final ZonedDateTime startDate,
                                                     final CurrencyType currencyType) {
        try {
            final long beggingOfTheDay = startDate.with(MIN).toInstant().toEpochMilli();
            final var or = currencyRepository.findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(PB_CASH, currencyType, beggingOfTheDay)
                    .or(() -> fetchCurrencyRate(startDate, currencyType));
            return or;
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol PB Cash rate due to unknown error: [{}]", e.getCause(), e);
            return empty();
        }
    }

    //TODO MAKE async
    private Optional<CurrencyRates> fetchCurrencyRate(final ZonedDateTime startDate,
                                                      final CurrencyType currencyType) {

        return currencyAsyncService.getPbCashDayRates()
                .thenApply(r -> r.orElse(emptyList()))
                .thenApply(r -> r.stream().filter(cr -> isEq(cr.getBaseCcy(), UAH_STR) && isEq(cr.getCcy(), currencyType.toString())).findFirst())
                .thenApply(r -> r.map(cr -> currencyResponseMapper.mapPbCashCurrencyRates(startDate, currencyType, cr)))
                .thenApply(r -> r.map(currencyRepository::save))
                .join();
    }

    private boolean isEq(final String baseCcy, final String currency) {
        return ofNullable(baseCcy).orElse(EMPTY).equalsIgnoreCase(currency);
    }
}
