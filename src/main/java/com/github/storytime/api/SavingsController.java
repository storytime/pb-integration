package com.github.storytime.api;

import com.github.storytime.model.api.SavingsInfoAsJson;
import com.github.storytime.service.info.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class SavingsController {

    private final SavingsService savingsService;

    @Autowired
    public SavingsController(final SavingsService zenDiffService) {
        this.savingsService = zenDiffService;
    }

    @GetMapping(value = API_PREFIX + "/savings/{userId}/info", produces = TEXT_PLAIN_VALUE)
    public String getAllSavingsAsTable(@PathVariable("userId") long userId) {
        return savingsService.getAllSavingsAsTable(userId);
    }

    @GetMapping(value = API_PREFIX + "/savings/{userId}/json", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SavingsInfoAsJson> getAllSavingsAsJson(@PathVariable("userId") long userId) {
        return savingsService.getAllSavingsJson(userId);
    }
}