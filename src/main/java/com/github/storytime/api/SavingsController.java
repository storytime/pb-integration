package com.github.storytime.api;

import com.github.storytime.service.SavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class SavingsController {

    private final SavingsService savingsService;

    @Autowired
    public SavingsController(final SavingsService zenDiffService) {
        this.savingsService = zenDiffService;
    }

    @GetMapping(value = API_PREFIX + "/savings/{userId}/info", produces = TEXT_PLAIN_VALUE)
    public String getVersion(@RequestParam("userId") long userId) {
        return savingsService.getAllSavingsInfo(userId);
    }
}