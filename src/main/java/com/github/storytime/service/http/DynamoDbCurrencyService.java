package com.github.storytime.service.http;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.storytime.model.aws.AwsCurrencyRates;
import com.github.storytime.repository.AwsCurrencyRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbCurrencyService {

    private static final Logger LOGGER = getLogger(DynamoDbCurrencyService.class);

    @Autowired
    private final AwsCurrencyRepository awsCurrencyRepository;

    @Autowired
    public DynamoDbCurrencyService(final AwsCurrencyRepository awsCurrencyRepository) {
        this.awsCurrencyRepository = awsCurrencyRepository;
    }

    public Optional<AwsCurrencyRates> getRateFromDynamo(final Map<String, AttributeValue> eav,
                                                        final long startDate) {
        final var st = createSt();

        try {
            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("currencyType = :type and currencySource =:source")
                    .withExpressionAttributeValues(eav);

            final Optional<AwsCurrencyRates> maybeRate = awsCurrencyRepository.findByTypeSourceAndDate(scanExpression)
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

    public AwsCurrencyRates saveRate(AwsCurrencyRates rate) {
        final var st = createSt();
        try {
            AwsCurrencyRates savedRate = awsCurrencyRepository.saveRate(rate);
            LOGGER.debug("Saved user dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), savedRate.getId());
            return savedRate;
        } catch (Exception e) {
            LOGGER.debug("Error saving db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return rate;
        }
    }

}
