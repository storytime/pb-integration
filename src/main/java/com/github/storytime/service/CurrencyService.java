package com.github.storytime.service;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.storytime.mapper.response.AwsCurrencyResponseMapper;
import com.github.storytime.model.AwsCurrencySource;
import com.github.storytime.model.aws.AwsCurrencyRates;
import com.github.storytime.service.async.CurrencyAsyncService;
import com.github.storytime.service.http.DynamoDbCurrencyService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static com.github.storytime.config.props.CacheNames.CURRENCY_CACHE;
import static com.github.storytime.config.props.Constants.*;
import static java.lang.Math.abs;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_DOWN;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalTime.MIN;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class CurrencyService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);
    private final CurrencyAsyncService currencyAsyncService;
    private final AwsCurrencyResponseMapper awsCurrencyResponseMapper;
    private final DynamoDbCurrencyService dynamoDbCurrencyService;
    private final Executor cfThreadPool;

    @Autowired
    public CurrencyService(final CurrencyAsyncService currencyAsyncService,
                           final AwsCurrencyResponseMapper awsCurrencyResponseMapper,
                           final DynamoDbCurrencyService dynamoDbCurrencyService,
                           final Executor cfThreadPool) {

        this.currencyAsyncService = currencyAsyncService;
        this.awsCurrencyResponseMapper = awsCurrencyResponseMapper;
        this.dynamoDbCurrencyService = dynamoDbCurrencyService;
        this.cfThreadPool = cfThreadPool;
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

    @Cacheable(CURRENCY_CACHE)
    public Optional<AwsCurrencyRates> pbUsdCashDayRates(final ZonedDateTime startDate,
                                                        final String currencyType) {
        final var st = createSt();
        try {
            LOGGER.debug("Getting PB rate: [{}] - started", currencyType);
            final long beggingOfTheDay = startDate.with(MIN).toInstant().toEpochMilli();
            //final var rate = awsCurrencyRepository.getAllRates(PB_CASH, currencyType, beggingOfTheDay)
            final var rate = fetchCurrencyRate(beggingOfTheDay, AwsCurrencySource.PB_CASH, currencyType)
                    .thenApply(dbRate -> dbRate.or(() -> fetchCurrencyRate(startDate, currencyType)))
                    .join();
            LOGGER.debug("Getting PB rate: [{}], time: [{}] - finish", currencyType, getTimeAndReset(st));
            return rate;
        } catch (Exception e) {
            LOGGER.error("Cannot get PB Cash rate, time: [{}] due to unknown error: [{}] - error", getTimeAndReset(st), e.getCause(), e);
            return empty();
        }
    }

    private CompletableFuture<Optional<AwsCurrencyRates>> fetchCurrencyRate(final long startDate,
                                                                            final String source,
                                                                            final String type) {

        LOGGER.debug("Fetching rate from dynamo db - start");
        final Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":type", new AttributeValue().withS(type));
        eav.put(":source", new AttributeValue().withS(source));

        return supplyAsync(() -> dynamoDbCurrencyService.getRateFromDynamo(eav, startDate), cfThreadPool);
    }

    private Optional<AwsCurrencyRates> fetchCurrencyRate(final ZonedDateTime startDate,
                                                         final String currencyType) {
        return currencyAsyncService.getPbCashDayRates()
                .thenApply(r -> r.orElse(emptyList()))
                .thenApply(r -> r.stream().filter(cr -> isEq(cr.getBaseCcy(), UAH_STR) && isEq(cr.getCcy(), currencyType.toString())).findFirst())
                .thenApply(r -> r.map(cr -> awsCurrencyResponseMapper.mapPbCashCurrencyRates(startDate, currencyType, cr)))
                .thenApply(r -> r.map(dynamoDbCurrencyService::saveRate))
                .join();
    }

    private boolean isEq(final String baseCcy, final String currency) {
        return ofNullable(baseCcy).orElse(EMPTY).equalsIgnoreCase(currency);
    }
}
