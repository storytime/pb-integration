package com.github.storytime.api;

import com.github.storytime.service.YnabSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class YnabSyncController {

    private final YnabSyncService ynabSyncService;

    @Autowired
    public YnabSyncController(final YnabSyncService ynabSyncService) {
        this.ynabSyncService = ynabSyncService;
    }

    @GetMapping(value = API_PREFIX + "/ynab/{userId}/sync", produces = TEXT_PLAIN_VALUE)
    public HttpStatus getVersion(@PathVariable("userId") long userId) {
        return ynabSyncService.syncTransactions(userId);
    }
}