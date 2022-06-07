package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.AwsUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AwsUserRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<AwsUser> getAllUsers() {
        return dynamoDBMapper.scan(AwsUser.class, new DynamoDBScanExpression());
    }

    public AwsUser findById(String id) {
        return dynamoDBMapper.load(AwsUser.class, id);
    }

    public AwsUser save(AwsUser updatedUser) {
        dynamoDBMapper.save(updatedUser);
        return updatedUser;
    }

}
