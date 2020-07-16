package com.github.storytime.service;

import com.github.storytime.mapper.response.CurrencyResponseMapper;
import com.github.storytime.model.currency.pb.archive.PbRatesResponse;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencyType;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.repository.CurrencyRepository;
import com.github.storytime.service.http.CurrencyHttpService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CURRENCY_SCALE;
import static com.github.storytime.function.FunctionUtils.logAndGetEmpty;
import static com.github.storytime.model.db.inner.CurrencySource.NBU;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CurrencyService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);
    private final DateService dateService;
    private final CurrencyRepository currencyRepository;
    private final Executor cfThreadPool;
    private final CurrencyResponseMapper currencyResponseMapper;
    private final CurrencyHttpService currencyHttpService;

    @Autowired
    public CurrencyService(final DateService dateService,
                           final Executor cfThreadPool,
                           final CurrencyResponseMapper currencyResponseMapper,
                           final CurrencyHttpService currencyHttpService,
                           final CurrencyRepository currencyRepository) {

        this.dateService = dateService;
        this.cfThreadPool = cfThreadPool;
        this.currencyRepository = currencyRepository;
        this.currencyHttpService = currencyHttpService;
        this.currencyResponseMapper = currencyResponseMapper;
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

    public Optional<CurrencyRates> nbuPrevMouthLastBusinessDayRate(final Statement s, final String timeZone) {
        try {
            final ZonedDateTime lastDay = dateService.getPrevMouthLastBusiness(s, timeZone);
            final var date = lastDay.with(LocalTime.MIN).toInstant().toEpochMilli();
            return currencyRepository
                    .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(NBU, USD, date)
                    .or(() -> supplyAsync(getNbuCurrencyRates(lastDay), cfThreadPool).join());
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol NBU rate due to unknown error");
            return empty();
        }
    }

    public Optional<CurrencyRates> pbUsdCashDayRates(final ZonedDateTime startDate, final CurrencyType currencyType) {
        try {
            final long beggingOfTheDay = startDate.with(LocalTime.MIN).toInstant().toEpochMilli();
            return currencyRepository
                    .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(PB_CASH, currencyType, beggingOfTheDay)
                    .or(() -> supplyAsync(getPbCashDayRates(startDate, currencyType), cfThreadPool).join());
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol PB Cash rate due to unknown error");
            return empty();
        }
    }

    private Supplier<Optional<CurrencyRates>> getPbCashDayRates(final ZonedDateTime now, final CurrencyType currencyType) {
        return () -> currencyHttpService.pullPbCashRate()
                .flatMap(response -> currencyResponseMapper.mapPbCashCurrencyRates(now, currencyType, response))
                .or(logAndGetEmpty(LOGGER, ERROR, "No info about PB cash rate at all!"));
    }

    private Supplier<Optional<CurrencyRates>> getNbuCurrencyRates(final ZonedDateTime lastDay) {
        return () -> currencyHttpService.pullMinfinRatesForDate(dateService.toMinfinFormat(lastDay)) // todo what is fail?
                .flatMap(response -> {
                    final BigDecimal rate = new BigDecimal(response.getUsd().getAsk());
                    return Optional.of(currencyRepository.save(currencyResponseMapper.buildRate(NBU, lastDay, USD, rate, rate)));
                })
                .or(() -> currencyHttpService.pullPbRatesForDate(dateService.toPbFormat(lastDay)) // try second source
                        .map(PbRatesResponse::getExchangeRate)
                        .flatMap(rates -> currencyResponseMapper.mapNbuCurrencyRates(lastDay, rates)))
                .or(logAndGetEmpty(LOGGER, ERROR, "No info about NBU prev mouth last business day rate at all!"));
    }
}
