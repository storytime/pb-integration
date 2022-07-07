package com.github.storytime.service.http;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.storytime.model.aws.CurrencyRates;
import com.github.storytime.repository.CurrencyRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbCurrencyService {

    private static final Logger LOGGER = getLogger(DynamoDbCurrencyService.class);
    private final CurrencyRepository currencyRepository;

    @Autowired
    public DynamoDbCurrencyService(final CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Optional<CurrencyRates> getRateFromDynamo(final Map<String, AttributeValue> eav,
                                                     final long startDate) {
        final var st = createSt();

        try {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("currencyType = :type and currencySource =:source")
                    .withExpressionAttributeValues(eav);

            final Optional<CurrencyRates> maybeRate = currencyRepository.findByTypeSourceAndDate(scanExpression)
                    .stream()
                    .filter(x -> x.getDateTime() == startDate)
                    .findFirst();

            LOGGER.info("Pulled rate dynamo db time: [{}] - finish", getTimeAndReset(st));
            return maybeRate;
        } catch (Exception e) {
            LOGGER.error("Can not get rate dynamo db time: [{}] - error", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }

    public CurrencyRates saveRate(final CurrencyRates rate) {
        final var st = createSt();
        try {
            CurrencyRates savedRate = currencyRepository.saveRate(rate);
            LOGGER.debug("Saved rate dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), savedRate.getId());
            return savedRate;
        } catch (Exception e) {
            LOGGER.debug("Error saving rate to db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return rate;
        }
    }

}
