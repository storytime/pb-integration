package com.github.storytime.service;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.config.props.Constants;
import com.github.storytime.model.currency.minfin.MinfinResponse;
import com.github.storytime.model.currency.minfin.Usd;
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
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.CURRENCY_SCALE;
import static com.github.storytime.config.props.Constants.UAH;
import static com.github.storytime.model.db.inner.CurrencySource.NBU;
import static com.github.storytime.model.db.inner.CurrencySource.PB_CASH;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Arrays.asList;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;
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

    public BigDecimal convertDivide(Double from, Double to) {
        final BigDecimal cardSum = valueOf(abs(from));
        final BigDecimal operationSum = valueOf(to);
        return cardSum.divide(operationSum, HALF_UP).setScale(CURRENCY_SCALE, HALF_DOWN);
    }


    public BigDecimal convertDivide(Float from, BigDecimal rate) {
        final BigDecimal cardSum = valueOf(abs(from));
        return cardSum.divide(rate, CURRENCY_SCALE, HALF_UP).setScale(CURRENCY_SCALE, HALF_DOWN);
    }

    public Optional<CurrencyRates> nbuPrevMouthLastBusinessDayRate(Statement s, String timeZone) {

        final ZonedDateTime lastDay = dateService.getPrevMouthLastBusiness(s, timeZone);
        final long date = lastDay.toInstant().toEpochMilli();
        return currencyRepository
                .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(NBU, CurrencyType.USD, date)
                .or(() -> supplyAsync(getNbuCurrencyRates(lastDay), cfThreadPool).join());
    }

    public Optional<CurrencyRates> pbCashDayRates(Statement s, String timeZone) {
        final ZonedDateTime startDate = dateService.getPbStatementZonedDateTime(timeZone, s.getTrandate());
        return currencyRepository
                .findCurrencyRatesByCurrencySourceAndCurrencyTypeAndDate(PB_CASH, CurrencyType.USD, startDate.toInstant().toEpochMilli())
                .or(() -> supplyAsync(getPbCashDayRates(startDate), cfThreadPool).join());
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
            LOGGER.info("Pulling NBU currency form external service for date:[{}]", lastDay);

            //todo what is fail?
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

    private CurrencyRates buildNbuRate(CurrencySource cs, ZonedDateTime date, BigDecimal rate) {
        return new CurrencyRates()
                .setCurrencySource(cs)
                .setCurrencyType(CurrencyType.USD)
                .setDate(date.toInstant().toEpochMilli())
                .setBuyRate(rate)
                .setSellRate(rate);
    }

    private Optional<MinfinResponse> pullMinfinRatesForDate(String lastBusinessDay) {
        try {
            return ofNullable(restTemplate.getForEntity(customConfig.getMinExchangeUrl() + lastBusinessDay, MinfinResponse.class).getBody());
        } catch (Exception e) {
            LOGGER.error("Cannot get NBU USD minfin rate for date:[{}], reason:[{}]", lastBusinessDay, e.getMessage());
            return empty();
        }
    }

    private Optional<List<CashResponse>> pullPbCashRate() {
        try {
            final ResponseEntity<CashResponse[]> forEntity = restTemplate.getForEntity(customConfig.getPbCashUrl(), CashResponse[].class);
            return of(asList(ofNullable(forEntity.getBody()).orElse(new CashResponse[]{})));
        } catch (Exception e) {
            LOGGER.error("Cannot get pb cash rate reason:[{}]", e.getMessage());
            return empty();
        }
    }

    private Optional<PbRatesResponse> pullPbRatesForDate(String lastBusinessDay) {
        try {
            return ofNullable(restTemplate.getForEntity(customConfig.getPbExchangeUrl() + lastBusinessDay, PbRatesResponse.class).getBody());
        } catch (Exception e) {
            LOGGER.error("Cannot get NBU USD PB rate for date:[{}], reason:[{}]", lastBusinessDay, e.getMessage());
            return empty();
        }
    }
}
