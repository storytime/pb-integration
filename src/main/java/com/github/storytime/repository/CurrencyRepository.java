package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.CurrencyRates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CurrencyRepository {

    private final DynamoDBMapper dynamoDBMapper;

    @Autowired
    public CurrencyRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public List<CurrencyRates> findByTypeSourceAndDate(final DynamoDBScanExpression scanExpression) {
        return dynamoDBMapper.scan(CurrencyRates.class, scanExpression);
    }

    public CurrencyRates saveRate(final CurrencyRates rate) {
        dynamoDBMapper.save(rate);
        return rate;
    }

}
