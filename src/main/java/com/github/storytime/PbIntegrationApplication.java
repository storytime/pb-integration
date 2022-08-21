package com.github.storytime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
@EnableWebFlux
public class PbIntegrationApplication {

    public static void main(final String[] args) {
        SpringApplication.run(PbIntegrationApplication.class, args);
    }
}
