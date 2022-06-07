package com.github.storytime.service;

import com.github.storytime.config.AwsSqsConfig;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Component;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class AwsSqsPublisherService {

    private static final Logger LOGGER = getLogger(CurrencyService.class);

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final AwsSqsConfig awsSqsConfig;

    @Autowired
    public AwsSqsPublisherService(final QueueMessagingTemplate awsQueueMessagingTemplate,
                                  final AwsSqsConfig awsSqsConfig) {
        this.queueMessagingTemplate = awsQueueMessagingTemplate;
        this.awsSqsConfig = awsSqsConfig;
    }

    public void publishFinishMessage() {
        var uuid = randomUUID().toString();
        final var st = createSt();
        try {
            LOGGER.info("Going to push message to SQS: [{}] - starting...", uuid);
            queueMessagingTemplate.convertAndSend(awsSqsConfig.getShutdownQueue(), uuid);
            LOGGER.info("Pushed to SQS done: [{}], time: [{}] - done", uuid, getTimeAndReset(st));
        } catch (Exception e) {
            LOGGER.error("Cannot push to SQL: [{}], time: [{}] - error", uuid, getTimeAndReset(st), e);
        }
    }
}