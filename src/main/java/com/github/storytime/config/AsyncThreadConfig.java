package com.github.storytime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class AsyncThreadConfig {

    private final CustomConfig customConfig;

    @Autowired
    public AsyncThreadConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @Bean
    public TaskExecutor threadPoolTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(customConfig.getAsyncCorePoolSize());
        executor.setMaxPoolSize(customConfig.getAsyncMaxPoolSize());
        executor.setThreadNamePrefix(customConfig.getAsyncThreadPrefix());
        executor.initialize();
        return executor;
    }

}