package com.github.storytime.api;

import com.github.storytime.model.api.PbZenReconcileResponse;
import com.github.storytime.service.info.ReconcileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class ReconcileController {

    private final ReconcileService reconcileService;

    @Autowired
    public ReconcileController(final ReconcileService reconcileService) {
        this.reconcileService = reconcileService;
    }


    @GetMapping(value = API_PREFIX + "/reconcile/{userId}/pb", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<PbZenReconcileResponse>> reconcilePbZen(@PathVariable("userId") final String userId) {
        return reconcileService.reconcilePbJson(userId);
    }
}