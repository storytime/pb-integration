package com.github.storytime.router;


import com.github.storytime.service.info.VersionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static com.github.storytime.config.props.RouteNames.VERSION;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration(proxyBeanMethods = false)
public class VersionRouter {

    @Bean
    public RouterFunction<ServerResponse> route(final VersionService versionService) {
        return RouterFunctions.route(GET(VERSION).and(accept(TEXT_PLAIN)), versionService::readVersionMono);
    }
}