package com.github.storytime.service.http;

import com.github.storytime.model.aws.AwsPbStatement;
import com.github.storytime.repository.AwsStatementRepository;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class DynamoDbStatementService {

    private static final Logger LOGGER = getLogger(DynamoDbStatementService.class);

    @Autowired
    private AwsStatementRepository awsStatementRepository;

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


    public List<AwsPbStatement> saveStatements(List<AwsPbStatement> statementList) {
        final var st = createSt();
        try {
            awsStatementRepository.save(statementList);
            LOGGER.debug("Saved statements db time: [{}], id: [{}] - finish", getTimeAndReset(st), statementList.size());
            return statementList;
        } catch (Exception e) {
            LOGGER.debug("Saved statements db time: [{}], amount [{}] - finish", getTimeAndReset(st), e);
            return Collections.emptyList();
        }
    }
}
