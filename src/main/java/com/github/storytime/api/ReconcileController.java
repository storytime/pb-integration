package com.github.storytime.api;

import com.github.storytime.service.info.ReconcileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.github.storytime.config.props.Constants.API_PREFIX;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
public class ReconcileController {

    private final ReconcileService reconcileService;

    @Autowired
    public ReconcileController(final ReconcileService reconcileService) {
        this.reconcileService = reconcileService;
    }

    @GetMapping(value = API_PREFIX + "/reconcile/{userId}/{budgetName}/info/{year}/{mouth}", produces = TEXT_PLAIN_VALUE)
    public String reconcileByDate(@PathVariable("userId") long userId,
                                  @PathVariable("budgetName") String budgetName,
                                  @PathVariable("year") int year,
                                  @PathVariable("mouth") int mouth) {
        return reconcileService.reconcileTableByDate(userId, budgetName, year, mouth);
    }

    @GetMapping(value = API_PREFIX + "/reconcile/{userId}/{budgetName}/info", produces = TEXT_PLAIN_VALUE)
    public String reconcile(@PathVariable("userId") long userId, @PathVariable("budgetName") String budgetName) {
        return reconcileService.reconcileTableDefault(userId, budgetName);
    }
}