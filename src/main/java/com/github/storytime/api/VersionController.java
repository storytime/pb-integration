package com.github.storytime.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.github.storytime.config.Constants.*;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class VersionController {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class);

    @RequestMapping(value = API_PREFIX + "/version", produces = TEXT_PLAIN_VALUE, method = GET)
    public String getVersion() {
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(END_LINE_SEPARATOR)).concat(END_LINE_SEPARATOR);
        } catch (final Exception e) {
            LOGGER.error("Cannot read version", e);
            return N_A;
        }
    }
}