package com.github.storytime.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter signatureErrorCounter() {
        return Metrics.counter("signature_error_counter");
    }

    @Bean
    public Counter newTransactionsCounter() {
        return Metrics.counter("new_transactions_counter");
    }

    @Bean
    public Counter pbRequestTimeCounter() {
        return Metrics.counter("new_transactions_counter");
    }

}
