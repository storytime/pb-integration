package com.github.storytime.service;

import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.model.pb.jaxb.statement.response.ok.Response;
import com.github.storytime.service.http.DynamoDbStatementService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class AwsStatementService {

    private static final Logger LOGGER = LogManager.getLogger(AwsStatementService.class);

    private final DynamoDbStatementService dynamoDbStatementService;

    private final Executor cfThreadPool;

    @Autowired
    public AwsStatementService(DynamoDbStatementService dynamoDbStatementService, Executor cfThreadPool) {
        this.dynamoDbStatementService = dynamoDbStatementService;
        this.cfThreadPool = cfThreadPool;
    }

    public static String generateUniqString(Response.Data.Info.Statements.Statement pbSt) {
        return pbSt.getAppcode() + pbSt.getTerminal() + pbSt.getCardamount() + pbSt.getAmount();
    }


    public CompletableFuture<AwsPbStatement> getAllStatementsByUser(String userId) {
        return supplyAsync(() -> dynamoDbStatementService.getAllStatementsForUser(userId), cfThreadPool);
    }

    public CompletableFuture<List<AwsPbStatement>> saveAll(final List<AwsPbStatement> statementList) {
        return supplyAsync(() -> dynamoDbStatementService.saveAllStatements(statementList), cfThreadPool);
    }

    public CompletableFuture<Optional<AwsPbStatement>> save(final AwsPbStatement statement) {
        return supplyAsync(() -> dynamoDbStatementService.save(statement), cfThreadPool);
    }

}
