package com.github.storytime.config;


import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.other.RequestLoggerInterceptor;
import com.github.storytime.repository.CustomPayeeRepository;
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

    @Value("${http.connect.timeout}")
    private int httpConnectTimeout;

    @Value("${http.request.timeout}")
    private int httpRequestTimeout;

    @Value("#{'${zen.sync.force.fetch.items}'.split(',')}")
    private Set<String> zenSyncForceFetchItemsValues;

    @Bean
    public SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(httpConnectTimeout);
        simpleClientHttpRequestFactory.setReadTimeout(httpRequestTimeout);
        return simpleClientHttpRequestFactory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory simpleClientHttpRequestFactory) {
        final RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        restTemplate.setInterceptors(singletonList(new RequestLoggerInterceptor()));
        return restTemplate;
    }

    @Bean
    public Set<String> pbTransferInfo() {
        return new HashSet<>();
    }

    @Bean
    public Set<CustomPayee> customPayeeValues(final CustomPayeeRepository customPayeeRepository) {
        return new HashSet<>(customPayeeRepository.findAll());
    }

    @Bean
    public Set<String> zenSyncForceFetchItems() {
        return new HashSet<>(zenSyncForceFetchItemsValues);
    }
}
