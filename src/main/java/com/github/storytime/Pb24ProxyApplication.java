package com.github.storytime;

import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.apache.logging.log4j.LogManager.getLogger;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Pb24ProxyApplication {

    private static final Logger LOGGER = getLogger(Pb24ProxyApplication.class);

    public static void main(final String[] args) {
        LOGGER.info("================= Starting APP ==================");
        SpringApplication.run(Pb24ProxyApplication.class, args);
        LOGGER.info("================= APP Started ==================");
    }
}
