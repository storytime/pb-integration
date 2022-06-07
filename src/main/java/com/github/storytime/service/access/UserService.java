//package com.github.storytime.service.access;
//
//import com.github.storytime.model.api.ms.AppUser;
//import com.github.storytime.model.aws.AwsUser;
//import com.github.storytime.service.async.UserMsAsyncService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//import static com.github.storytime.config.props.CacheNames.*;
//import static java.util.concurrent.CompletableFuture.supplyAsync;
//
//@Service
//public class UserService {
//
//    private final UserMsAsyncService userMsAsyncService;
//    private final Executor cfThreadPool;
//
//    @Autowired
//    public UserService(final UserMsAsyncService userMsAsyncService,   final Executor cfThreadPool) {
//        this.userMsAsyncService = userMsAsyncService;
//        this.cfThreadPool = cfThreadPool;
//
//    }
//
//    @Cacheable(USERS_CACHE)
//    public CompletableFuture<List<AppUser>> findAllUsersAsync() {
//        return userMsAsyncService.getAllUsers();
//    }
//
//    @Cacheable(USERS_PERMANENT_CACHE)
//    public CompletableFuture<List<AppUser>> findAllAsyncForCache() {
//        return userMsAsyncService.getAllUsers();
//    }
//
//    @Cacheable(USER_PERMANENT_CACHE)
//    public CompletableFuture<Optional<AppUser>> findUserByIdAsyncCache(final long userId) {
//        return userMsAsyncService.getByIdAsync(userId);
//    }
//
//
//}
