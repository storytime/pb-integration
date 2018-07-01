package com.github.storytime.service;

import com.github.storytime.config.Constants;
import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.currency.minfin.MinfinResponse;
import com.github.storytime.model.currency.minfin.Usd;
import com.github.storytime.model.currency.pb.archive.ExchangeRateItem;
import com.github.storytime.model.currency.pb.archive.PbRatesResponse;
import com.github.storytime.model.currency.pb.cash.CashResponse;
import com.github.storytime.model.db.CurrencyRates;
import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;
import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import com.github.storytime.repository.CurrencyRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.github.storytime.config.Constants.CURRENCY_SCALE;
import static com.github.storytime.config.Constants.UAH;
import static com.github.storytime.model.db.inner.CurrencySource.NBU;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static java.lang.Math.abs;
import static java.math.BigDecimal.*;
import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CurrencyService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);
    private final DateService dateService;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public CurrencyService(final DateService dateService,
                           final RestTemplate restTemplate,
                           final CustomConfig customConfig,
                           final CurrencyRepository currencyRepository) {

        this.dateService = dateService;
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
        this.currencyRepository = currencyRepository;
    }

    public BigDecimal convertDivide(Float from, Float to) {
        final BigDecimal cardSum = valueOf(abs(from));
        final BigDecimal operationSum = valueOf(to);
        return cardSum.divide(operationSum, ROUND_HALF_UP).setScale(CURRENCY_SCALE, ROUND_DOWN);
    }


    public BigDecimal convertDivide(Float from, BigDecimal rate) {
        final BigDecimal cardSum = valueOf(abs(from));
        return cardSum.divide(rate, CURRENCY_SCALE, ROUND_HALF_UP).setScale(CURRENCY_SCALE, ROUND_DOWN);
    }

    public Optional<CurrencyRates> nbuPrevMouthLastBusinessDayRate(Statement s, String timeZone) {

        final ZonedDateTime lastDay = dateService.getPrevMouthLastBusiness(s, timeZone);
        final long date = lastDay.toInstant().toEpochMilli();
        return currencyRepository
                .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(NBU, CurrencyType.USD, date)
                .map(Optional::of)
                .orElseGet(() -> supplyAsync(getNbuCurrencyRates(lastDay)).join());
    }

    public Optional<CurrencyRates> pbCashDayRates(String timeZone) {

        final ZonedDateTime now = now(of(timeZone));
        return currencyRepository
                .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(PB_CASH, CurrencyType.USD, now.toInstant().toEpochMilli())
                .map(Optional::of)
                .orElseGet(() -> supplyAsync(getPbCashDayRates(now)).join());
    }

    private Supplier<Optional<CurrencyRates>> getPbCashDayRates(ZonedDateTime now) {
        return () -> {
            LOGGER.info("Pulling PB Cash currency");

            final Optional<List<CashResponse>> cashResponses = pullPbCashRate();
            if (cashResponses.isPresent()) {
                final Optional<String> maybeCashRate = cashResponses
                        .get()
                        .stream()
                        .filter(cr -> cr.getBaseCcy().equalsIgnoreCase(UAH)
                                && cr.getCcy().equalsIgnoreCase(Constants.USD))
                        .findFirst()
                        .map(CashResponse::getBuy);

                if (maybeCashRate.isPresent()) {
                    final CurrencyRates dbRate = buildNbuRate(PB_CASH, now, new BigDecimal(maybeCashRate.get()));
                    return of(currencyRepository.save(dbRate));
                } else {
                    LOGGER.warn("Cannot get PB cash rate form response");
                    return empty();
                }
            }
            LOGGER.error("No info about PB cash rate all!");
            return empty();
        };
    }

    private Supplier<Optional<CurrencyRates>> getNbuCurrencyRates(ZonedDateTime lastDay) {
        return () -> {
            LOGGER.info("Pulling NBU currency form external service for date: {}", lastDay);

            final Optional<MinfinResponse> minFinRates = pullMinfinRatesForDate(dateService.toMinfinFormat(lastDay));
            if (minFinRates.isPresent()) {
                final Optional<String> maybeUsdRate = minFinRates.map(MinfinResponse::getUsd).map(Usd::getAsk);
                if (maybeUsdRate.isPresent()) {
                    final CurrencyRates dbRate = buildNbuRate(NBU, lastDay, new BigDecimal(maybeUsdRate.get()));
                    return of(currencyRepository.save(dbRate));
                } else {
                    LOGGER.warn("Cannot find NBU currency in minfin response");
                    return empty();
                }
            }

            // sometimes there is no info at minfin so second try
            final Optional<PbRatesResponse> pbRates = pullPbRatesForDate(dateService.toPbFormat(lastDay));
            if (pbRates.isPresent()) {
                final Optional<Double> maybeUsdRate = pbRates.get()
                        .getExchangeRate()
                        .stream()
                        .filter(r -> r.getBaseCurrency().equalsIgnoreCase(UAH) &&
                                r.getCurrency().equalsIgnoreCase(Constants.USD))
                        .findFirst()
                        .map(ExchangeRateItem::getPurchaseRateNB);

                if (maybeUsdRate.isPresent()) {
                    final CurrencyRates dbRate = buildNbuRate(NBU, lastDay, valueOf(maybeUsdRate.get()));
                    return of(currencyRepository.save(dbRate));
                } else {
                    LOGGER.warn("Cannot find NBU currency in PB response");
                    return empty();
                }
            }
            LOGGER.error("No info about NBU rate all!");
            return empty();
        };
    }

    private CurrencyRates buildNbuRate(CurrencySource cs, ZonedDateTime lastDay, BigDecimal rate) {
        return new CurrencyRates()
                .setCurrencySource(cs)
                .setCurrencyType(CurrencyType.USD)
                .setDate(lastDay.toInstant().toEpochMilli())
                .setBuyRate(rate)
                .setSellRate(rate);
    }

    private Optional<MinfinResponse> pullMinfinRatesForDate(String lastBusinessDay) {
        try {
            return ofNullable(restTemplate.getForEntity(customConfig.getMinExchangeUrl() + lastBusinessDay, MinfinResponse.class).getBody());
        } catch (Exception e) {
            LOGGER.error("Cannot get NBU USD minfin rate for date: {}, reason: {}", lastBusinessDay, e.getMessage());
            return empty();
        }
    }

    private Optional<List<CashResponse>> pullPbCashRate() {
        try {
            final ResponseEntity<CashResponse[]> forEntity = restTemplate.getForEntity(customConfig.getPbCashUrl(), CashResponse[].class);
            return ofNullable(Arrays.asList(forEntity.getBody()));
        } catch (Exception e) {
            LOGGER.error("Cannot get pb cash rate reason: {}", e.getMessage());
            return empty();
        }
    }

    private Optional<PbRatesResponse> pullPbRatesForDate(String lastBusinessDay) {
        try {
            final PbRatesResponse body = restTemplate
                    .getForEntity(customConfig.getPbExchangeUrl() + lastBusinessDay, PbRatesResponse.class).getBody();

            return body.getExchangeRate().isEmpty() ? empty() : of(body);
        } catch (Exception e) {
            LOGGER.error("Cannot get NBU USD PB rate for date: {}, reason: {}", lastBusinessDay, e.getMessage());
            return empty();
        }
    }
}
