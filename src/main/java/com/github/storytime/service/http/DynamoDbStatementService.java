package com.github.storytime.service.http;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.repository.AwsStatementRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbStatementService {

    private static final Logger LOGGER = getLogger(DynamoDbStatementService.class);


    private final AwsStatementRepository awsStatementRepository;

    @Autowired
    public DynamoDbStatementService(final AwsStatementRepository awsStatementRepository) {
        this.awsStatementRepository = awsStatementRepository;
    }

    public List<AwsPbStatement> getAllStatements() {
        final var st = createSt();
        try {
            final var allStatement = awsStatementRepository.getAllStatement();
            LOGGER.debug("Pulled statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allStatement.size());
            return allStatement;
        } catch (Exception e) {
            LOGGER.debug("Error to fetch users from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Collections.emptyList();
        }
    }


    public AwsPbStatement getAllStatementsForUser(String userId) {
        final var st = createSt();
        try {

            final Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":id", new AttributeValue().withS(userId));

            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("userId = :id")
                    .withExpressionAttributeValues(eav)
                    .withLimit(1);

            final var allStatement = awsStatementRepository.getAllByUser(scanExpression);
            LOGGER.debug("Pulled user statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allStatement.size());
            return allStatement.isEmpty() ? AwsPbStatement.builder().userId(userId).alreadyPushed(Collections.<String>emptySet()).build() : allStatement.stream().findFirst().get();
        } catch (Exception e) {
            LOGGER.debug("Error to fetch users  statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return AwsPbStatement.builder().userId(userId).alreadyPushed(Collections.<String>emptySet()).build();
        }
    }

    public List<AwsPbStatement> saveAllStatements(List<AwsPbStatement> statementList) {
        final var st = createSt();
        try {
            awsStatementRepository.saveAll(statementList);
            LOGGER.debug("Saved statements db time: [{}], id: [{}] - finish", getTimeAndReset(st), statementList.size());
            return statementList;
        } catch (Exception e) {
            LOGGER.debug("Saved statements db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Collections.emptyList();
        }
    }

    public Optional<AwsPbStatement> save(AwsPbStatement awsPbStatement) {
        final var st = createSt();
        try {
            awsStatementRepository.save(awsPbStatement);
            LOGGER.debug("Saved statement db time: [{}], user id: [{}] - finish", getTimeAndReset(st), awsPbStatement.getUserId());
            return Optional.of(awsPbStatement);
        } catch (Exception e) {
            LOGGER.debug("Saved statements db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Optional.empty();
        }
    }
}
