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
public class PbAccountsHttpService {

    private static final Logger LOGGER = LogManager.getLogger(PbAccountsHttpService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public PbAccountsHttpService(final RestTemplate restTemplate,
                                 final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }

    public Optional<ResponseEntity<String>> pullPbAccounts(final Request requestToBank) {
        try {
            final String pbAccountsUrl = customConfig.getPbAccountsUrl();
            LOGGER.debug("Going to call:[{}]", pbAccountsUrl);
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ResponseEntity<String>> response = of(restTemplate.postForEntity(pbAccountsUrl, requestToBank, String.class));
            st.stop();
            LOGGER.debug("Receive bank account response, execution time:[{}] sec", st.getTotalTimeSeconds());
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot do bank request:[{}]", e.getMessage());
            return empty();
        }
    }
}
