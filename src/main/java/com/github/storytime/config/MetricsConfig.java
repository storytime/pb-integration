package com.github.storytime.config;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer pbRequestTimeTimer() {
        return Metrics.timer("pb_request_time_timer");
    }
}
