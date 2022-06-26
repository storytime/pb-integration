package com.github.storytime.api;

import com.github.storytime.model.aws.CustomPayee;
import com.github.storytime.service.utils.CustomPayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CustomPayeeController {

    private final CustomPayeeService customPayeeService;

    @Autowired
    public CustomPayeeController(final CustomPayeeService customPayeeService) {
        this.customPayeeService = customPayeeService;
    }

    @GetMapping(value = API_PREFIX + "/payee/{userId}", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<List<CustomPayee>> getCustomerPayee(@PathVariable("userId") final String userId) {
        return customPayeeService.getPayeeByUserId(userId);
    }

}