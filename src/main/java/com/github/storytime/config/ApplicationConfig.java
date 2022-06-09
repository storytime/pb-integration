package com.github.storytime.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.List.of;

@Configuration
public class ApplicationConfig {

    @Bean
    public List<String> configsToPrint() {
        return of(
                "applicationConfig: [classpath:/application.properties]",
                "class path resource [date.format.properties]",
                "class path resource [custom.properties]"
        );
    }
}
