package com.github.storytime.config;


import com.github.storytime.model.ExpiredPbStatement;
import com.github.storytime.model.db.CustomPayee;
import com.github.storytime.repository.CustomPayeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Configuration
public class ApplicationConfig {

    @Value("#{'${zen.sync.force.fetch.items}'.split(',')}")
    private Set<String> zenSyncForceFetchItemsValues;

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
    public Set<ExpiredPbStatement> pushedPbZenTransactionStorage() {
        return new HashSet<>();
    }

    @Bean
    public List<String> configsToPrint() {
        return asList(
                "applicationConfig: [classpath:/application.properties]",
                "class path resource [date.format.properties]",
                "class path resource [custom.properties]"
        );
    }

}
