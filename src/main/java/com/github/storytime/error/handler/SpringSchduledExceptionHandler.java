package com.github.storytime.error.handler;

import org.apache.logging.log4j.Logger;
import org.springframework.util.ErrorHandler;

import static org.apache.logging.log4j.LogManager.getLogger;

public class SpringSchduledExceptionHandler implements ErrorHandler {

    private static final Logger LOGGER = getLogger(SpringSchduledExceptionHandler.class);

    @Override
    public void handleError(final Throwable t) {
        LOGGER.error("Error in @Scheduled ", t);
    }
}