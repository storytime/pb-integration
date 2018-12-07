package com.github.storytime.error.handler;

import org.apache.logging.log4j.Logger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

import static org.apache.logging.log4j.LogManager.getLogger;

public class SpringAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger LOGGER = getLogger(SpringAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(final Throwable throwable, final Method method, final Object... obj) {
        LOGGER.error("Error in @Async ", throwable);
    }

}