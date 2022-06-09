package com.github.storytime.service;

import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.service.http.DynamoDbUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.CacheNames.USERS_CACHE;
import static com.github.storytime.config.props.CacheNames.USER_PERMANENT_CACHE;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class AwsUserAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(AwsUserAsyncService.class);

    @Autowired
    private DynamoDbUserService dynamoDbUserService;

    @Autowired
    private Executor cfThreadPool;

    //    @Cacheable(USERS_PERMANENT_CACHE)
    public CompletableFuture<List<AwsUser>> getAllUsers() {
        LOGGER.debug("Fetching all users from dynamo db - start");
        return supplyAsync(dynamoDbUserService::getAwsAllUsers, cfThreadPool);
    }

    @Cacheable(USER_PERMANENT_CACHE)
    public CompletableFuture<Optional<AwsUser>> getById(String id) {
        LOGGER.debug("Fetching user from dynamo - start");
        return supplyAsync(() -> dynamoDbUserService.getById(id), cfThreadPool);
    }

    @CacheEvict(value = USERS_CACHE, allEntries = true)
    public CompletableFuture<Optional<AwsUser>> updateUser(final AwsUser appUser) {
        return supplyAsync(() -> dynamoDbUserService.saveUser(appUser), cfThreadPool);
    }

}
