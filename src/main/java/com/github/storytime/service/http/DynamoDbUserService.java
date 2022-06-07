package com.github.storytime.service.http;

import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.repository.AwsUserRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbUserService {

    private static final Logger LOGGER = getLogger(DynamoDbUserService.class);

    @Autowired
    private AwsUserRepository awsUserRepository;

    @Autowired
    public DynamoDbUserService(final AwsUserRepository awsUserRepository) {
        this.awsUserRepository = awsUserRepository;
    }

    public List<AwsUser> getAwsAllUsers() {
        final var st = createSt();
        try {
            final var allUsers = awsUserRepository.getAllUsers();
            LOGGER.debug("Pulled from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allUsers.size());
            return allUsers;
        } catch (Exception e) {
            LOGGER.error("Error to fetch users from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Collections.emptyList();
        }
    }

    public Optional<AwsUser> getById(String id) {
        final var st = createSt();
        try {
            final var user = awsUserRepository.findById(id);
            LOGGER.debug("Pulled user dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), id);
            return Optional.of(user);
        } catch (Exception e) {
            LOGGER.error("Error to fetch user from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }

    public Optional<AwsUser> saveUser(AwsUser user) {
        final var st = createSt();
        try {
            awsUserRepository.save(user);
            LOGGER.debug("Saved user dynamo db time: [{}], id: [{}] - finish", getTimeAndReset(st), user.getId());
            return Optional.of(user);
        } catch (Exception e) {
            LOGGER.debug("Error saving db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }
}
