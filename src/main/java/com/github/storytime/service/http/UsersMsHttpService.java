//package com.github.storytime.service.http;
//
//import com.github.storytime.config.AppServicesConfig;
//import com.github.storytime.model.api.ms.AppUser;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Optional;
//
//import static com.github.storytime.STUtils.createSt;
//import static com.github.storytime.STUtils.getTime;
//import static java.lang.String.valueOf;
//import static java.util.Collections.emptyList;
//import static java.util.List.of;
//import static java.util.Objects.requireNonNull;
//import static java.util.Optional.empty;
//import static java.util.Optional.ofNullable;
//import static org.apache.logging.log4j.LogManager.getLogger;
//
//@Service
//public class UsersMsHttpService {
//
//    private static final Logger LOGGER = getLogger(UsersMsHttpService.class);
//    private final RestTemplate restTemplate;
//    private final AppServicesConfig appServicesConfig;
//
//    @Autowired
//    public UsersMsHttpService(final RestTemplate restTemplate,
//                              final AppServicesConfig appServicesConfig) {
//        this.restTemplate = restTemplate;
//        this.appServicesConfig = appServicesConfig;
//    }
//
//    public Optional<AppUser> getUserByIdAsync(final long id) {
//        final var st = createSt();
//        try {
//            final var url = appServicesConfig.getUsersMsGetByIdUrl().concat(valueOf(id));
//            final var forEntity = restTemplate.getForEntity(url, AppUser.class);
//            final var body = ofNullable(forEntity.getBody());
//            LOGGER.debug("Fetching user from ms id: [{}], time: [{}] - finish!", id, getTime(st));
//            return body;
//        } catch (Exception e) {
//            LOGGER.error("Error user from ms id: [{}], time: [{}] reason: [{}] - error", id, getTime(st), e.getMessage(), e);
//            return empty();
//        }
//    }
//
//    public List<AppUser> getAllUsers() {
//        final var st = createSt();
//        try {
//            final var forEntity = restTemplate.getForEntity(appServicesConfig.getUsersMmGetALlUrl(), AppUser[].class);
//            final var appUsers = of(requireNonNull(forEntity.getBody()));
//            LOGGER.debug("Fetching all users from ms, time: [{}] - finish!", getTime(st));
//            return appUsers;
//        } catch (Exception e) {
//            LOGGER.error("Error fetching all users from ms, time: [{}], reason: [{}] - error", getTime(st), e.getMessage(), e);
//            return emptyList();
//        }
//    }
//
//    public Optional<AppUser> saveUser(final AppUser updateUser) {
//        final var st = createSt();
//        try {
//            final var exchange = restTemplate.exchange(appServicesConfig.getSaveUserUrl(), HttpMethod.PUT, new HttpEntity<>(updateUser), Void.class);
//            final var statusCode = exchange.getStatusCode();
//            if (statusCode.is2xxSuccessful()) {
//                LOGGER.debug("Saved user via ms id: [{}], time: [{}] - finish", updateUser.getId(), getTime(st));
//                return Optional.of(updateUser);
//            } else {
//                LOGGER.debug("Saved user via ms id: [{}], time: [{}] - finish with error", updateUser.getId(), getTime(st));
//                return empty();
//            }
//        } catch (Exception e) {
//            LOGGER.debug("Cannot user via ms id: [{}], time: [{}], error: [{}] - error", updateUser.getId(), getTime(st), e.getCause(), e);
//            return empty();
//        }
//    }
//}
