package com.github.storytime.service.async;

import com.github.storytime.service.http.SqsAsyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class SqsAsyncPublisherService {

    private static final Logger LOGGER = getLogger(SqsAsyncPublisherService.class);
    private final SqsAsyncService sqsAsyncService;
    private final Executor cfThreadPool;

    @Autowired
    public SqsAsyncPublisherService(SqsAsyncService sqsAsyncService, Executor cfThreadPool) {
        this.sqsAsyncService = sqsAsyncService;
        this.cfThreadPool = cfThreadPool;
    }

    public CompletableFuture<String> publishFinishMessage() {
        LOGGER.info("Going to push message to SQS - starting...");
        return supplyAsync(sqsAsyncService::publishFinishMessage, cfThreadPool);
    }

}