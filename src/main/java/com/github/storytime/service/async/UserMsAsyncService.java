//package com.github.storytime.service.async;
//
//import com.github.storytime.model.api.ms.AppUser;
//import com.github.storytime.service.http.UsersMsHttpService;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executor;
//
//import static java.util.concurrent.CompletableFuture.supplyAsync;
//
//@Service
//public class UserMsAsyncService {
//
//    private static final Logger LOGGER = LogManager.getLogger(UserMsAsyncService.class);
//
//    private final Executor cfThreadPool;
//    private final UsersMsHttpService usersMsHttpService;
//
//    @Autowired
//    public UserMsAsyncService(final UsersMsHttpService usersMsHttpService,
//                              final Executor cfThreadPool) {
//        this.usersMsHttpService = usersMsHttpService;
//        this.cfThreadPool = cfThreadPool;
//    }
//
//    public CompletableFuture<Optional<AppUser>> getByIdAsync(final long id) {
//        LOGGER.debug("Fetching user from ms id: [{}] - start", id);
//        return supplyAsync(() -> usersMsHttpService.getUserByIdAsync(id), cfThreadPool);
//    }
//
//    public CompletableFuture<List<AppUser>> getAllUsers() {
//        LOGGER.debug("Fetching all users from ms - start");
//        return supplyAsync(usersMsHttpService::getAllUsers, cfThreadPool);
//    }
//
//    public CompletableFuture<Optional<AppUser>> saveUser(final AppUser appUser) {
//        LOGGER.debug("Saving user via ms id: [{}] - start", appUser.getId());
//        return supplyAsync(() -> usersMsHttpService.saveUser(appUser), cfThreadPool);
//    }
//}
