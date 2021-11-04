package com.github.storytime.service.async;

import com.github.storytime.model.pb.jaxb.request.Request;
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

    private final Executor pool;
    private final PbStatementsHttpService pbStatementsHttpService;

    @Autowired
    public PbAsyncService(final PbStatementsHttpService pbStatementsHttpService,
                          final Executor cfThreadPool) {
        this.pbStatementsHttpService = pbStatementsHttpService;
        this.pool = cfThreadPool;
    }

    public CompletableFuture<Optional<ResponseEntity<String>>> pullPbTransactions(final Request requestToBank) {
        LOGGER.debug("Fetching bank transactions - started");
        return supplyAsync(() -> pbStatementsHttpService.pullPbTransactions(requestToBank), pool);
    }

    public CompletableFuture<Optional<ResponseEntity<String>>> pullPbAccounts(final Request requestToBank) {
        LOGGER.debug("Fetching bank accounts - started");
        return supplyAsync(() -> pbStatementsHttpService.pullPbAccounts(requestToBank), pool);
    }
}
