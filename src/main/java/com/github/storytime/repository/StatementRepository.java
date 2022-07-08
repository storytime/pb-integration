package com.github.storytime.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.github.storytime.model.aws.PbStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StatementRepository {

    private final DynamoDBMapper dynamoDBMapper;

    @Autowired
    public StatementRepository(final DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public List<PbStatement> getAllStatement() {
        return dynamoDBMapper.scan(PbStatement.class, new DynamoDBScanExpression());
    }

    public List<PbStatement> getAllByUser(final DynamoDBScanExpression scanExpression) {
        return dynamoDBMapper.scan(PbStatement.class, scanExpression);
    }

    public List<PbStatement> saveAll(final List<PbStatement> statementList) {
        dynamoDBMapper.save(statementList);
        return statementList;
    }

    public PbStatement save(final PbStatement pbStatements) {
        dynamoDBMapper.save(pbStatements);
        return pbStatements;
    }

}
