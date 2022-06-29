package com.github.storytime.service.http;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.storytime.model.aws.PbStatement;
import com.github.storytime.repository.StatementRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.storytime.config.props.Constants.DYNAMO_REQUEST_ID;
import static com.github.storytime.config.props.Constants.SEARCH_LIMIT;
import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbStatementService {

    private static final Logger LOGGER = getLogger(DynamoDbStatementService.class);
    private final StatementRepository statementRepository;

    @Autowired
    public DynamoDbStatementService(final StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public List<PbStatement> getAllStatements() {
        final var st = createSt();
        try {
            final var allStatement = statementRepository.getAllStatement();
            LOGGER.debug("Pulled statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allStatement.size());
            return allStatement;
        } catch (Exception e) {
            LOGGER.debug("Error to fetch statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return emptyList();
        }
    }

    public PbStatement getAllStatementsForUser(final String userId) {
        final var st = createSt();
        try {

            final Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(DYNAMO_REQUEST_ID, new AttributeValue().withS(userId));

            final DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withFilterExpression("userId = :id")
                    .withExpressionAttributeValues(eav)
                    .withLimit(SEARCH_LIMIT);

            final var allStatement = statementRepository.getAllByUser(scanExpression);
            LOGGER.debug("Pulled user statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), allStatement.size());
            return allStatement.isEmpty() ? PbStatement.builder().userId(userId).alreadyPushed(emptySet()).build() : allStatement.stream().findFirst().orElseThrow();
        } catch (Exception e) {
            LOGGER.debug("Error to fetch users statements from dynamo db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return PbStatement.builder().userId(userId).alreadyPushed(emptySet()).build();
        }
    }

    public List<PbStatement> saveAllStatements(final List<PbStatement> statementList) {
        final var st = createSt();
        try {
            statementRepository.saveAll(statementList);
            LOGGER.debug("Saved statements db time: [{}], id: [{}] - finish", getTimeAndReset(st), statementList.size());
            return statementList;
        } catch (Exception e) {
            LOGGER.debug("Saved statements db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return emptyList();
        }
    }

    public Optional<PbStatement> save(final PbStatement pbStatement) {
        final var st = createSt();
        try {
            statementRepository.save(pbStatement);
            LOGGER.debug("Saved statement db time: [{}], user id: [{}] - finish", getTimeAndReset(st), pbStatement.getUserId());
            return of(pbStatement);
        } catch (Exception e) {
            LOGGER.debug("Saved statements db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return empty();
        }
    }
}
