package com.github.storytime.config;

import com.github.storytime.error.handler.SpringAsyncExceptionHandler;
import com.github.storytime.error.handler.SpringScheduledExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
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
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(customConfig.getSchedulerCorePoolSize());
        scheduler.setThreadNamePrefix(customConfig.getSchedulerThreadPrefix());
        scheduler.setErrorHandler(new SpringScheduledExceptionHandler());
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public Executor cfThreadPool() {
        final ThreadPoolTaskExecutor cfExecutor = new ThreadPoolTaskExecutor();
        cfExecutor.setCorePoolSize(customConfig.getCfCorePoolSize());
        cfExecutor.setMaxPoolSize(customConfig.getCfMaxPoolSize());
        cfExecutor.setThreadNamePrefix(customConfig.getCfThreadPrefix());
        cfExecutor.initialize();
        return cfExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SpringAsyncExceptionHandler();
    }

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduledTaskExecutor());
    }
}