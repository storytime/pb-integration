package com.github.storytime.service.async;

import com.github.storytime.model.currency.pb.cash.CashResponse;
import com.github.storytime.service.http.CurrencyHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Component
public class CurrencyAsyncService {

    private final Executor cfThreadPool;
    private final CurrencyHttpService currencyHttpService;

    @Autowired
    public CurrencyAsyncService(final Executor cfThreadPool,
                                final CurrencyHttpService currencyHttpService) {

        this.cfThreadPool = cfThreadPool;
        this.currencyHttpService = currencyHttpService;
    }

    public CompletableFuture<Optional<List<CashResponse>>> getPbCashDayRates() {
        return supplyAsync(currencyHttpService::pullPbCashRate, cfThreadPool);
    }
}
