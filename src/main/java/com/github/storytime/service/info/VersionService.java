package com.github.storytime.service.info;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.config.props.Constants.N_A;
import static com.github.storytime.config.props.Constants.VERSION_PROPERTIES;
import static com.github.storytime.error.AsyncErrorHandlerUtil.logVersionCf;
import static com.github.storytime.service.util.STUtils.createSt;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.LF;

@Component
public class VersionService {

    private static final Logger LOGGER = LogManager.getLogger(VersionService.class);
    private final Executor cfThreadPool;

    @Autowired
    public VersionService(final Executor cfThreadPool) {
        this.cfThreadPool = cfThreadPool;
    }

    public CompletableFuture<String> readVersion() {
        LOGGER.debug("Version - started");
        var st = createSt();
        return supplyAsync(this::getString, cfThreadPool).whenComplete((r, e) -> logVersionCf(st, LOGGER, e));
    }

    public String getString() {
        try (final var is = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
            final var reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(joining(LF)).concat(LF).trim();
        } catch (final Exception e) {
            return N_A;
        }
    }
}
