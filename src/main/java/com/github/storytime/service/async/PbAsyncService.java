package com.github.storytime.service.async;

import com.github.storytime.model.pb.jaxb.request.Request;
import com.github.storytime.service.http.PbAccountsHttpService;
import com.github.storytime.service.http.PbStatementsHttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class PbAsyncService {

    private static final Logger LOGGER = LogManager.getLogger(PbAsyncService.class);

    private final Executor cfThreadPool;
    private final PbStatementsHttpService pbStatementsHttpService;
    private final PbAccountsHttpService pbAccountsHttpService;

    @Autowired
    public PbAsyncService(final PbStatementsHttpService pbStatementsHttpService,
                          final PbAccountsHttpService pbAccountsHttpService,
                          final Executor cfThreadPool) {
        this.pbStatementsHttpService = pbStatementsHttpService;
        this.cfThreadPool = cfThreadPool;
        this.pbAccountsHttpService = pbAccountsHttpService;
    }

    public CompletableFuture<Optional<ResponseEntity<String>>> pullPbTransactions(final Request requestToBank) {
        return supplyAsync(() -> pbStatementsHttpService.pullPbTransactions(requestToBank), cfThreadPool);
    }

    public CompletableFuture<Optional<ResponseEntity<String>>> pullPbAccounts(final Request requestToBank) {
        return supplyAsync(() -> pbAccountsHttpService.pullPbAccounts(requestToBank), cfThreadPool);
    }
}
