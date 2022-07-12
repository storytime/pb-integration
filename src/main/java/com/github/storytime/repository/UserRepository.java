package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final DynamoDBMapper dynamoDBMapper;

    @Autowired
    public UserRepository(final DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public List<AppUser> getAllUsers() {
        return dynamoDBMapper.scan(AppUser.class, new DynamoDBScanExpression());
    }

    public AppUser findById(final String id) {
        return dynamoDBMapper.load(AppUser.class, id);
    }

    public AppUser save(final AppUser updatedUser) {
        dynamoDBMapper.save(updatedUser);
        return updatedUser;
    }

}
