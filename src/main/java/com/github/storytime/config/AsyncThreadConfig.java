package com.github.storytime.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Executor;

@Configuration
public class AsyncThreadConfig implements AsyncConfigurer, SchedulingConfigurer {

    private final CustomConfig customConfig;

    @Autowired
    public AsyncThreadConfig(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @Override
    @Bean
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(customConfig.getAsyncCorePoolSize());
        executor.setMaxPoolSize(customConfig.getAsyncMaxPoolSize());
        executor.setThreadNamePrefix(customConfig.getAsyncThreadPrefix());
        executor.initialize();
        return executor;
    }


    @Bean
    public ThreadPoolTaskScheduler scheduledTaskExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(customConfig.getSchedulerCorePoolSize());
        scheduler.setThreadNamePrefix(customConfig.getSchedulerThreadPrefix());
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduledTaskExecutor());
    }
}