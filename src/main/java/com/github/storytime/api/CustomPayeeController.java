package com.github.storytime.api;

import com.github.storytime.model.aws.CustomPayee;
import com.github.storytime.service.misc.CustomPayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.CacheNames.CUSTOM_PAYEE_CACHE;
import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CustomPayeeController {

    private final CustomPayeeService customPayeeService;

    @Autowired
    public CustomPayeeController(final CustomPayeeService customPayeeService) {
        this.customPayeeService = customPayeeService;
    }

    @Cacheable(cacheNames = CUSTOM_PAYEE_CACHE, key = "#userId")
    @GetMapping(value = API_PREFIX + "/payee/{userId}", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<List<CustomPayee>> getCustomerPayee(@PathVariable("userId") final String userId) {
        return customPayeeService.getPayeeByUserId(userId);
    }


    @PutMapping(value = API_PREFIX + "/payee/{userId}", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<List<CustomPayee>> updateCustomerPayee(@PathVariable("userId") final String userId) {
        return customPayeeService.updatePayeeByUserId(userId);
    }

}