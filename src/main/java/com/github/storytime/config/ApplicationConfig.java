package com.github.storytime.config;


import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.List.of;

@Configuration
public class ApplicationConfig {

    @Bean
    public Set<Statement> pushedPbZenTransactionStorage() {
        return new HashSet<>();
    }

    @Bean
    public List<String> configsToPrint() {
        return of(
                "applicationConfig: [classpath:/application.properties]",
                "class path resource [date.format.properties]",
                "class path resource [custom.properties]"
        );
    }

}
