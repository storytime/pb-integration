package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.pb.jaxb.request.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

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
        try {
            final String pbTransactionsUrl = customConfig.getPbTransactionsUrl();
            LOGGER.debug("Going to call:[{}]", pbTransactionsUrl);
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ResponseEntity<String>> response = of(restTemplate.postForEntity(pbTransactionsUrl, requestToBank, String.class));
            st.stop();
            LOGGER.debug("Receive bank transactions response, execution time:[{}] sec", st.getTotalTimeSeconds());
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot do bank request:[{}]", e.getMessage());
            return empty();
        }
    }
}
