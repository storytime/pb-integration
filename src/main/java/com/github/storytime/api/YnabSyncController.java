package com.github.storytime.api;

import com.github.storytime.service.sync.YnabSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;

@RestController
public class YnabSyncController {

    private final YnabSyncService ynabSyncService;

    @Autowired
    public YnabSyncController(final YnabSyncService ynabSyncService) {
        this.ynabSyncService = ynabSyncService;
    }

    @GetMapping(value = API_PREFIX + "/ynab/{userId}/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getVersion(@PathVariable("userId") long userId) {
        return ynabSyncService.syncTransactions(userId);
    }
}