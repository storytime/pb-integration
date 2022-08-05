package com.github.storytime.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

@Configuration
public class DateFormatConfig {

    @Value("${iso}")
    private String iso;

    @Value("${private.bank}")
    private String pb;

    @Value("${zen}")
    private String zen;

    @Bean
    public DateTimeFormatter isoDateTimeFormatter() {
        return ofPattern(iso);
    }

    @Bean
    public DateTimeFormatter pbDateTimeFormatter() {
        return ofPattern(pb);
    }

    @Bean
    public DateTimeFormatter zenDateTimeFormatter() {
        return ofPattern(zen);
    }

}
