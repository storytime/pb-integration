package com.github.storytime.service.async;

import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.service.http.DynamoDbStatementService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class StatementAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(StatementAsyncService.class);
    private final DynamoDbStatementService dynamoDbStatementService;
    private final Executor cfThreadPool;

    @Autowired
    public StatementAsyncService(final DynamoDbStatementService dynamoDbStatementService, final Executor cfThreadPool) {
        this.dynamoDbStatementService = dynamoDbStatementService;
        this.cfThreadPool = cfThreadPool;
    }

    public CompletableFuture<PbStatement> getAllStatementsByUser(final String userId) {
        LOGGER.debug("Fetching all pushed statements for user: [{}] from dynamo - start", userId);
        return supplyAsync(() -> dynamoDbStatementService.getAllStatementsForUser(userId), cfThreadPool);
    }

    public CompletableFuture<Optional<PbStatement>> saveAll(final PbStatement statement, final String userId) {
        LOGGER.debug("Saving all pushed statements for user: [{}] from dynamo - start", userId);
        return supplyAsync(() -> dynamoDbStatementService.save(statement), cfThreadPool);
    }
}
