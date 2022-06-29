package com.github.storytime.service.http;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.repository.UserRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbUserService {

    private static final Logger LOGGER = getLogger(DynamoDbUserService.class);
    private final UserRepository userRepository;

    @Autowired
    public DynamoDbUserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AppUser> getAwsAllUsers() {
        final var st = createSt();
        try {
            final var allUsers = userRepository.getAllUsers();
            LOGGER.debug("Pulled from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allUsers.size());
            return allUsers;
        } catch (Exception e) {
            LOGGER.error("Error to fetch users from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Collections.emptyList();
        }
    }

    public Optional<AppUser> getById(final String id) {
        final var st = createSt();
        try {
            final var user = userRepository.findById(id);
            LOGGER.debug("Pulled user dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), id);
            return Optional.of(user);
        } catch (Exception e) {
            LOGGER.error("Error to fetch user from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }

    public Optional<AppUser> saveUser(final AppUser user) {
        final var st = createSt();
        try {
            userRepository.save(user);
            LOGGER.debug("Saved user dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), user.getId());
            return Optional.of(user);
        } catch (Exception e) {
            LOGGER.debug("Error saving db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }
}
