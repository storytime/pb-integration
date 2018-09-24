package com.github.storytime.config;


import com.github.storytime.model.ExpiredTransactionItem;
import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.other.RequestLoggerInterceptor;
import com.github.storytime.repository.CustomPayeeRepository;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

@Configuration
public class ApplicationConfig {

    @Value("${http.connect.timeout.millis}")
    private int httpConnectTimeoutMillis;

    @Value("${http.request.timeout.millis}")
    private int httpRequestTimeoutMillis;

    @Value("#{'${zen.sync.force.fetch.items}'.split(',')}")
    private Set<String> zenSyncForceFetchItemsValues;

    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(httpConnectTimeoutMillis);
        simpleClientHttpRequestFactory.setReadTimeout(httpRequestTimeoutMillis);
        return simpleClientHttpRequestFactory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory simpleClientHttpRequestFactory) {
        final RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        restTemplate.setInterceptors(singletonList(new RequestLoggerInterceptor()));
        return restTemplate;
    }

    @Bean
    public Set<String> pbTransferInfoStorage() {
        return new HashSet<>();
    }

    @Bean
    public Set<CustomPayee> customPayeeValuesStorage(final CustomPayeeRepository customPayeeRepository) {
        return new HashSet<>(customPayeeRepository.findAll());
    }

    @Bean
    public Set<String> zenSyncForceFetchItems() {
        return new HashSet<>(zenSyncForceFetchItemsValues);
    }

    @Bean
    public Set<ExpiredTransactionItem> pushedPbZenTransactionStorage() {
        return new HashSet<>();
    }

    @Bean
    public Timer testMetrics() {
        return Metrics.timer("my_test");
    }


}
