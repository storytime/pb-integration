package com.github.storytime.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.*;
import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class VersionController {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class);

    @GetMapping(value = API_PREFIX + "/version", produces = TEXT_PLAIN_VALUE)
    public String getVersion() {
        try {
            final String version = new String(readAllBytes(get(getSystemResource(VERSION_PROPERTIES).toURI())));
            LOGGER.info("Return version: {}", version);
            return version;
        } catch (final Exception e) {
            LOGGER.error("Cannot read version", e);
            return N_A;
        }
    }
}