package com.github.storytime.service;

import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.service.http.DynamoDbStatementService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class AwsStatementService {

    private static final Logger LOGGER = LogManager.getLogger(AwsStatementService.class);

    @Autowired
    private DynamoDbStatementService dynamoDbStatementService;

    @Autowired
    private Executor cfThreadPool;

    public CompletableFuture<List<AwsPbStatement>> getAllUsers() {
        LOGGER.debug("Fetching all statements from dynamo db - start");
        return supplyAsync(dynamoDbStatementService::getAllStatements, cfThreadPool);
    }

    public CompletableFuture<List<AwsPbStatement>> saveAll(final List<AwsPbStatement> statementList) {
        return supplyAsync(() -> dynamoDbStatementService.saveStatements(statementList), cfThreadPool);
    }

}
