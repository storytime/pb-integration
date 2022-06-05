package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.AwsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.github.storytime.config.props.CacheNames.USERS_CACHE;

@Repository
public class AwsUserRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Cacheable(USERS_CACHE)
    public List<AwsUser> getAllUsers() {
        return dynamoDBMapper.scan(AwsUser.class, new DynamoDBScanExpression());
    }

}
