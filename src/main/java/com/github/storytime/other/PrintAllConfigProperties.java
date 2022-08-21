package com.github.storytime.other;

import com.github.storytime.service.info.VersionService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static org.apache.logging.log4j.LogManager.getLogger;

@Component
public class PrintAllConfigProperties {

    private static final Logger LOGGER = getLogger(PrintAllConfigProperties.class);
    private final VersionService versionService;

    @Autowired
    public PrintAllConfigProperties(final VersionService versionService) {
        this.versionService = versionService;
    }

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        try {
         //   versionService.readVersion().thenAccept(v -> LOGGER.debug("========== Build date: [{}] ==========", v));
        } catch (Exception e) {
            LOGGER.error("Cannot print properties: [{}]", e.getMessage());
        }
    }
}