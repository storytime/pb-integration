package com.github.storytime.service.http;

import com.github.storytime.config.AwsSqsConfig;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class SqsAsyncService {

    private static final Logger LOGGER = getLogger(SqsAsyncService.class);
    private final QueueMessagingTemplate queueMessagingTemplate;
    private final AwsSqsConfig awsSqsConfig;

    public SqsAsyncService(final QueueMessagingTemplate queueMessagingTemplate,
                           final AwsSqsConfig awsSqsConfig) {
        this.queueMessagingTemplate = queueMessagingTemplate;
        this.awsSqsConfig = awsSqsConfig;
    }

    public String publishFinishMessage() {
        var uuid = randomUUID().toString();
        final var st = createSt();
        try {
            queueMessagingTemplate.convertAndSend(awsSqsConfig.getShutdownQueue(), uuid);
            LOGGER.info("Pushed to SQS done: [{}], time: [{}] - done", uuid, getTimeAndReset(st));
            return uuid;
        } catch (Exception e) {
            LOGGER.error("Cannot push to SQL: [{}], time: [{}] - error", uuid, getTimeAndReset(st), e);
            return uuid;
        }
    }
}
