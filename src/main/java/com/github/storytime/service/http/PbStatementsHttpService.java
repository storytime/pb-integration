package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.pb.jaxb.request.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTime;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Service
public class PbStatementsHttpService {

    private static final Logger LOGGER = LogManager.getLogger(PbStatementsHttpService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public PbStatementsHttpService(final RestTemplate restTemplate,
                                   final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }

    public Optional<ResponseEntity<String>> pullPbTransactions(final Request requestToBank) {
        final var st = createSt();
        try {
            final var pbTransactionsUrl = customConfig.getPbTransactionsUrl();
            final var response = of(restTemplate.postForEntity(pbTransactionsUrl, requestToBank, String.class));
            LOGGER.debug("Fetched bank transactions, time: [{}] - finish", getTime(st));
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch bank transactions, time: [{}], errors: [{}] - error", e.getMessage(), getTime(st), e);
            return empty();
        }
    }

    public Optional<ResponseEntity<String>> pullPbAccounts(final Request requestToBank) {
        final var st = createSt();
        try {
            final var pbAccountsUrl = customConfig.getPbAccountsUrl();
            final var response = of(restTemplate.postForEntity(pbAccountsUrl, requestToBank, String.class));
            LOGGER.debug("Fetched bank account, time: [{}] - finish", getTime(st));
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch bank account, time: [{}], errors: [{}] - error", e.getMessage(), getTime(st), e);
            return empty();
        }
    }
}
