package com.github.storytime;

import com.github.storytime.config.AwsSqsConfig;
import com.github.storytime.service.CurrencyService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.stereotype.Component;

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
        var uuid = java.util.UUID.randomUUID().toString();
        try {
            LOGGER.info("Going to push message to SQS: [{}] - starting...", uuid);
            queueMessagingTemplate.convertAndSend(awsSqsConfig.getShutdownQueue(), uuid);
            LOGGER.info("Going to push message to SQS: [{}] - done", uuid);
        } catch (Exception e) {
            LOGGER.error("Cannot push to SQL: [{}] - error", uuid, e);
        }
    }
}