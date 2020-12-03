package com.github.storytime.service.access;

import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.service.async.UserMsAsyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    private final UserMsAsyncService userMsAsyncService;

    @Autowired
    public UserService(final UserMsAsyncService userMsAsyncService) {
        this.userMsAsyncService = userMsAsyncService;
    }

    public CompletableFuture<List<AppUser>> findAllAsync() {
        return userMsAsyncService.getAllUsers();
    }

    public CompletableFuture<Optional<AppUser>> findUserByIdAsync(final long userId) {
        return userMsAsyncService.getByIdAsync(userId);
    }

    public CompletableFuture<Optional<AppUser>> updateUserLastZenSyncTime(final AppUser appUser) {
        return userMsAsyncService.saveUser(appUser);
    }
}
