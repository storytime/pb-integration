package com.github.storytime.other;

import com.github.storytime.service.info.VersionService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PrintAllConfigProperties {

    private static final Logger LOGGER = getLogger(PrintAllConfigProperties.class);
    private final List<String> configsToPrint;
    private final VersionService versionService;

    @Autowired
    public PrintAllConfigProperties(final List<String> configsToPrint, final VersionService versionService) {
        this.configsToPrint = configsToPrint;
        this.versionService = versionService;
    }

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        try {
            printActiveProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
            versionService.readVersion().thenAccept(v -> LOGGER.debug("========== Build date: [{}] ==========", v));
        } catch (Exception e) {
            LOGGER.error("Cannot print properties: [{}]", e.getMessage());
        }
    }

    private void printActiveProperties(ConfigurableEnvironment env) {
        configsToPrint.forEach(configName -> {
            LOGGER.debug("==========[{}] PROPERTIES ==========", configName);
            final List<MapPropertySource> propertySources = new ArrayList<>();
            env.getPropertySources().forEach(it -> {
                if (it instanceof MapPropertySource && it.getName().contains(configName)) {
                    propertySources.add((MapPropertySource) it);
                }
            });

            propertySources.stream()
                    .map(propertySource -> propertySource.getSource().keySet())
                    .flatMap(Collection::stream)
                    .distinct()
                    .sorted()
                    .forEach(key -> LOGGER.debug("\t{}={}", key, env.getProperty(key)));

        });
    }
}