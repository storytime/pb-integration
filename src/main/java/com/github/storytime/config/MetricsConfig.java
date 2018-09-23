package com.github.storytime.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Timer testMetrics(){

        final MeterRegistry registry = new SimpleMeterRegistry();
        Metrics.addRegistry(registry);

        final Timer register = Timer.builder("my")
                .description("custom function timer")
                .tags("custom", "timer")
                .register(registry);

        Counter counter = Counter
                .builder("instance")
                .description("indicates instance count of the object")
                .tags("dev", "performance")
                .register(registry);

        counter.increment(2.0);

        return register;
    }
}
