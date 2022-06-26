package com.github.storytime.service.async;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.service.http.DynamoDbUserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.CacheNames.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class UserAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(UserAsyncService.class);

    private final DynamoDbUserService dynamoDbUserService;
    private final Executor cfThreadPool;

    @Autowired
    public UserAsyncService(final DynamoDbUserService dynamoDbUserService, final Executor cfThreadPool) {
        this.dynamoDbUserService = dynamoDbUserService;
        this.cfThreadPool = cfThreadPool;
    }

    @Cacheable(USERS_PERMANENT_CACHE)
    public CompletableFuture<List<AppUser>> getAllUsers() {
        LOGGER.debug("Fetching all users from dynamo db - start");
        return supplyAsync(dynamoDbUserService::getAwsAllUsers, cfThreadPool);
    }

    @Cacheable(cacheNames = USER_PERMANENT_CACHE, key = "#id")
    public CompletableFuture<Optional<AppUser>> getById(final String id) {
        LOGGER.debug("Fetching user: [{}] from dynamo - start", id);
        return supplyAsync(() -> dynamoDbUserService.getById(id), cfThreadPool);
    }

    @Caching(evict = {
            @CacheEvict(value = USERS_CACHE, allEntries = true),
            @CacheEvict(value = USER_PERMANENT_CACHE, key = "#appUser.id"),
            @CacheEvict(value = CUSTOM_PAYEE_CACHE, key = "#appUser.id")
    })
    public CompletableFuture<Optional<AppUser>> updateUser(final AppUser appUser) {
        LOGGER.debug("Fetching user: [{}] from dynamo - start", appUser.getId());
        return supplyAsync(() -> dynamoDbUserService.saveUser(appUser), cfThreadPool);
    }
}
