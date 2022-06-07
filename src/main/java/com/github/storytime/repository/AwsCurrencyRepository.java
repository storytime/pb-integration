package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.AwsCurrencyRates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AwsCurrencyRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<AwsCurrencyRates> findByTypeSourceAndDate(DynamoDBScanExpression scanExpression) {
        return dynamoDBMapper.scan(AwsCurrencyRates.class, scanExpression);
    }

    public AwsCurrencyRates saveRate(AwsCurrencyRates rate) {
        dynamoDBMapper.save(rate);
        return rate;
    }

}
