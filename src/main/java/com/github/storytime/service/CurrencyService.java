package com.github.storytime.service;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.currency.minfin.MinfinResponse;
import com.github.storytime.model.currency.pb.archive.ExchangeRateItem;
import com.github.storytime.model.currency.pb.archive.PbRatesResponse;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;
import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.repository.CurrencyRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.function.FunctionUtils.logAndGetEmpty;
import static com.github.storytime.model.db.inner.CurrencySource.NBU;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static com.github.storytime.model.db.inner.CurrencyType.USD;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CurrencyService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);
    private final DateService dateService;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;
    private final Executor cfThreadPool;

    @Autowired
    public CurrencyService(final DateService dateService,
                           final RestTemplate restTemplate,
                           final CustomConfig customConfig,
                           final Executor cfThreadPool,
                           final CurrencyRepository currencyRepository) {

        this.dateService = dateService;
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
        this.cfThreadPool = cfThreadPool;
        this.currencyRepository = currencyRepository;
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
        return () -> pullPbCashRate()
                .flatMap(response -> mapPbCashCurrencyRates(now, currencyType, response))
                .or(logAndGetEmpty(LOGGER, ERROR, "No info about PB cash rate at all!"));
    }

    private Optional<CurrencyRates> mapPbCashCurrencyRates(final ZonedDateTime now,
                                                           final CurrencyType currencyType,
                                                           final List<CashResponse> response) {
        return response.stream()
                .filter(cr -> ofNullable(cr.getBaseCcy()).orElse(EMPTY).equalsIgnoreCase(UAH_STR) &&
                        ofNullable(cr.getCcy()).orElse(EMPTY).equalsIgnoreCase(currencyType.toString()))
                .findFirst()
                .map(cr -> buildRate(PB_CASH, now, currencyType, new BigDecimal(cr.getBuy()), new BigDecimal(cr.getSale())))
                .map(currencyRepository::save);
    }

    private Supplier<Optional<CurrencyRates>> getNbuCurrencyRates(final ZonedDateTime lastDay) {
        return () -> pullMinfinRatesForDate(dateService.toMinfinFormat(lastDay)) // todo what is fail?
                .flatMap(response -> {
                    final BigDecimal rate = new BigDecimal(response.getUsd().getAsk());
                    return Optional.of(currencyRepository.save(buildRate(NBU, lastDay, USD, rate, rate)));
                })
                .or(() -> pullPbRatesForDate(dateService.toPbFormat(lastDay)) // try second source
                        .map(PbRatesResponse::getExchangeRate)
                        .flatMap(rates -> mapNbuCurrencyRates(lastDay, rates)))
                .or(logAndGetEmpty(LOGGER, ERROR, "No info about NBU prev mouth last business day rate at all!"));
    }

    private Optional<CurrencyRates> mapNbuCurrencyRates(final ZonedDateTime lastDay, final List<ExchangeRateItem> rates) {
        return rates.stream()
                .filter(cr ->
                        ofNullable(cr.getBaseCurrency()).orElse(EMPTY).equalsIgnoreCase(UAH_STR) &&
                                ofNullable(cr.getCurrency()).orElse(EMPTY).equalsIgnoreCase(USD_STR))
                .findFirst()
                .map(ExchangeRateItem::getPurchaseRateNB)
                .map(rate -> buildRate(NBU, lastDay, USD, valueOf(rate), valueOf(rate)))
                .map(currencyRepository::save);
    }

    private CurrencyRates buildRate(final CurrencySource cs,
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

    private Optional<MinfinResponse> pullMinfinRatesForDate(final String lastBusinessDay) {
        try {
            LOGGER.info("Pulling NBU currency form external service for date:[{}]", lastBusinessDay);
            return ofNullable(restTemplate.getForEntity(customConfig.getMinExchangeUrl() + lastBusinessDay, MinfinResponse.class).getBody());
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol NBU USD minfin rate for date:[{}], reason:[{}]", lastBusinessDay, e.getMessage());
            return empty();
        }
    }

    private Optional<PbRatesResponse> pullPbRatesForDate(final String lastBusinessDay) {
        try {
            LOGGER.info("Pulling NBU currency form external service for date:[{}]", lastBusinessDay);
            return ofNullable(restTemplate.getForEntity(customConfig.getPbExchangeUrl() + lastBusinessDay, PbRatesResponse.class).getBody());
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol NBU USD PB rate for date:[{}], reason:[{}]", lastBusinessDay, e.getMessage());
            return empty();
        }
    }

    private Optional<List<CashResponse>> pullPbCashRate() {
        try {
            LOGGER.info("Pulling PB Cash currency");
            final ResponseEntity<CashResponse[]> forEntity = restTemplate.getForEntity(customConfig.getPbCashUrl(), CashResponse[].class);
            return Optional.of(List.of(ofNullable(forEntity.getBody()).orElse(new CashResponse[]{})));
        } catch (Exception e) {
            LOGGER.error("Cannot getZenCurrencySymbol PB cash rate reason:[{}]", e.getMessage());
            return empty();
        }
    }
}
