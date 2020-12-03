package com.github.storytime.api;

import com.github.storytime.service.info.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class VersionController {

    private final VersionService versionService;

    @Autowired
    public VersionController(final VersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping(value = API_PREFIX + "/version", produces = TEXT_PLAIN_VALUE)
    public CompletableFuture<String> getVersion() {
        return versionService.readVersion();
    }
}