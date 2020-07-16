package com.github.storytime.api;

import com.github.storytime.service.sync.YnabSyncService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static java.time.Instant.now;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class YnabSyncController {

    private static final Logger LOGGER = getLogger(YnabSyncController.class);
    private final YnabSyncService ynabSyncService;

    @Autowired
    public YnabSyncController(final YnabSyncService ynabSyncService) {
        this.ynabSyncService = ynabSyncService;
    }

    @GetMapping(value = API_PREFIX + "/ynab/{userId}/sync", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> pushToYnab(@PathVariable("userId") long userId) {
        LOGGER.debug("Calling YNAB sync for user:[{}], with default", userId);
        final var clientSyncTime = now().getEpochSecond();
        return ynabSyncService.startSync(userId, clientSyncTime);
    }

    @GetMapping(value = API_PREFIX + "/ynab/{userId}/sync/{startFrom}", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<String> pushToYnabFrom(@PathVariable("userId") long userId, @PathVariable("startFrom") long startFrom) {
        LOGGER.debug("Calling YNAB sync for user:[{}], with epoch time: [{}]", userId, startFrom);
        return ynabSyncService.startSync(userId, startFrom);
    }
}