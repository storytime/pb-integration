package com.github.storytime.service.info;

import com.github.storytime.STUtils;
import com.github.storytime.api.VersionController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static com.github.storytime.config.props.Constants.N_A;
import static com.github.storytime.config.props.Constants.VERSION_PROPERTIES;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.LF;

@Component
public class VersionService {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class);
    private final Executor cfThreadPool;


    @Autowired
    public VersionService(final Executor cfThreadPool) {
        this.cfThreadPool = cfThreadPool;
    }

    public CompletableFuture<String> readVersion() {
        var st = createSt();
        return supplyAsync(() -> {
            LOGGER.debug("Version - start");
            try (final var is = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
                final var reader = new BufferedReader(new InputStreamReader(is));
                final var resp = reader.lines().collect(joining(LF)).concat(LF).trim();
                LOGGER.debug("Version time: [{}] - finish", getTime(st));
                return resp;
            } catch (final Exception e) {
                LOGGER.debug("Version time: [{}] - error", getTime(st), e);
                return N_A;
            }
        }, cfThreadPool);
    }
}
