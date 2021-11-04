package com.github.storytime.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

import static com.github.storytime.config.props.CacheNames.*;

@Configuration
@EnableCaching
@EnableScheduling
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        final List<Cache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache(USERS_CACHE));
        caches.add(new ConcurrentMapCache(USERS_PERMANENT_CACHE));
        caches.add(new ConcurrentMapCache(USER_PERMANENT_CACHE));
        caches.add(new ConcurrentMapCache(CURRENCY_CACHE));
        caches.add(new ConcurrentMapCache(MERCHANT_CACHE));
        caches.add(new ConcurrentMapCache(TR_TAGS_DIFF));
        caches.add(new ConcurrentMapCache(CUSTOM_PAYEE));
        caches.add(new ConcurrentMapCache(VERSION));
        caches.add(new ConcurrentMapCache(OUT_DATA_BY_MONTH));
        caches.add(new ConcurrentMapCache(IN_DATA_BY_MONTH));
        caches.add(new ConcurrentMapCache(OUT_DATA_BY_YEAR));
        caches.add(new ConcurrentMapCache(IN_DATA_BY_YEAR));
        caches.add(new ConcurrentMapCache(OUT_DATA_BY_QUARTER));
        caches.add(new ConcurrentMapCache(IN_DATA_BY_QUARTER));
        cacheManager.setCaches(caches);
        return cacheManager;
    }


}