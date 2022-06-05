package com.github.storytime.service;

import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.repository.AwsUserRepository;
import com.github.storytime.service.async.UserMsAsyncService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class AwsUserService {

    private static final Logger LOGGER = LogManager.getLogger(UserMsAsyncService.class);

    @Autowired
    private AwsUserRepository awsUserRepository;

    @Autowired
    private Executor cfThreadPool;

    public CompletableFuture<List<AwsUser>> getAllUsers() {
        LOGGER.debug("Fetching all users from ms - start");
        return supplyAsync(() -> awsUserRepository.getAllUsers(), cfThreadPool);
    }

}
