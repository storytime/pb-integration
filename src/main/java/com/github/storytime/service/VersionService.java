package com.github.storytime.service;

import com.github.storytime.api.VersionController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.github.storytime.config.props.Constants.N_A;
import static com.github.storytime.config.props.Constants.VERSION_PROPERTIES;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.LF;

@Component
public class VersionService {

    private static final Logger LOGGER = LogManager.getLogger(VersionController.class);

    public String readVersion() {
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES)) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(joining(LF)).concat(LF).trim();
        } catch (final Exception e) {
            LOGGER.error("Cannot read version", e);
            return N_A;
        }
    }
}
