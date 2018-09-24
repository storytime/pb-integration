package com.github.storytime.api;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static com.github.storytime.config.props.Constants.*;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class VersionController {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class);

    public final Timer testMetrics;
    public final Counter testCounter;

    @Autowired
    public VersionController(Timer testMetrics, Counter testCounter) {
        this.testMetrics = testMetrics;
        this.testCounter = testCounter;
    }

    @GetMapping(value = API_PREFIX + "/version", produces = TEXT_PLAIN_VALUE)
    public String getVersion() {
        testMetrics.record(10, TimeUnit.MILLISECONDS);
        testCounter.increment(1);
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            final String version = reader.lines().collect(joining(END_LINE_SEPARATOR)).concat(END_LINE_SEPARATOR).trim();
            LOGGER.info("Return version: {} ", version);
            return version;
        } catch (final Exception e) {
            LOGGER.error("Cannot read version", e);
            return N_A;
        }
    }
}