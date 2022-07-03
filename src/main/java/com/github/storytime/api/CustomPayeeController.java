package com.github.storytime.api;

import com.github.storytime.model.api.CustomPayee;
import com.github.storytime.service.misc.CustomPayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping(value = API_PREFIX + "/payee/{userId}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<Void>> updateCustomerPayee(@RequestBody final List<CustomPayee> toUpdateList,
                                                                       @PathVariable("userId") final String userId) {
        return customPayeeService.updatePayeeByUserId(userId, toUpdateList);
    }

}