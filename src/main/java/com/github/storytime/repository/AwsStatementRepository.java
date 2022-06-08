package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.AwsPbStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AwsStatementRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public List<AwsPbStatement> getAllStatement() {
        return dynamoDBMapper.scan(AwsPbStatement.class, new DynamoDBScanExpression());
    }

    public List<AwsPbStatement> save(List<AwsPbStatement> statementList) {
        dynamoDBMapper.save(statementList);
        return statementList;
    }

}
