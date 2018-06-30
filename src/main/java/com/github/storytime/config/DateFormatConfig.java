package com.github.storytime.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

@Configuration
@PropertySource("classpath:date.format.properties")
public class DateFormatConfig {

    @Value("${minfin}")
    private String minfin;

    @Value("${iso}")
    private String iso;

    @Value("${private.bank}")
    private String pb;

    @Value("${zen}")
    private String zen;

    @Bean
    public DateTimeFormatter minfinDateTimeFormatter() {
        return ofPattern(minfin);
    }

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
