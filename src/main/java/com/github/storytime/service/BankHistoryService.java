package com.github.storytime.service;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.exception.PbSignatureException;
import com.github.storytime.model.db.User;
import com.github.storytime.model.jaxb.history.request.Request;
import com.github.storytime.model.jaxb.history.response.ok.Response;
import com.github.storytime.model.jaxb.history.response.ok.Response.Data.Info.Statements.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;

@Service
public class BankHistoryService {

    private static final Logger LOGGER = LogManager.getLogger(BankHistoryService.class);
    private final Unmarshaller jaxbHistoryErrorUnmarshaller;
    private final Unmarshaller jaxbHistoryOkUnmarshaller;
    private final RestTemplate restTemplate;
    private final DateService dateService;
    private final CustomConfig customConfig;

    @Autowired
    public BankHistoryService(final RestTemplate restTemplate,
                              final DateService dateService,
                              final Unmarshaller jaxbHistoryOkUnmarshaller,
                              final CustomConfig customConfig,
                              final Unmarshaller jaxbHistoryErrorUnmarshaller) {
        this.restTemplate = restTemplate;
        this.dateService = dateService;
        this.customConfig = customConfig;
        this.jaxbHistoryOkUnmarshaller = jaxbHistoryOkUnmarshaller;
        this.jaxbHistoryErrorUnmarshaller = jaxbHistoryErrorUnmarshaller;
    }

    public Optional<ResponseEntity<String>> pullPbTransactions(final Request requestToBank) {
        try {
            final String pbTransactionsUrl = customConfig.getPbTransactionsUrl();
            LOGGER.debug("Going to call: {}", pbTransactionsUrl);
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ResponseEntity<String>> response = of(restTemplate.postForEntity(pbTransactionsUrl, requestToBank, String.class));
            st.stop();
            LOGGER.debug("Receive bank response, request execution time: {} sec", st.getTotalTimeSeconds());
            return response;
        } catch (Exception e) {
            LOGGER.error("Cannot do bank request: {}", e.getMessage());
            return empty();
        }
    }

    public List<Statement> getPbTransactionsFromBody(ResponseEntity<String> responseEntity) {

        final String body = ofNullable(responseEntity.getBody()).orElse("");
        if (body.contains(customConfig.getPbBankSignature())) {
            throw new PbSignatureException("Invalid signature");
        }

        try {
            if (!body.contains("signature")) { // is error response, wrong ip etc
                final com.github.storytime.model.jaxb.history.response.error.Response error =
                        (com.github.storytime.model.jaxb.history.response.error.Response) jaxbHistoryErrorUnmarshaller.unmarshal(new StringReader(body));
                LOGGER.error("Bank return response with error: {}", error.getData().getError().getMessage());
                return emptyList();
            }

            final Response parsedResponse = (Response) jaxbHistoryOkUnmarshaller.unmarshal(new StringReader(body));
            return ofNullable(parsedResponse.getData())
                    .map(Response.Data::getInfo)
                    .map(Response.Data.Info::getStatements)
                    .map(Response.Data.Info.Statements::getStatement)
                    .orElse(emptyList());
        } catch (Exception e) {
            LOGGER.error("Cannot parse bank response: {}", e.getMessage(), e);
            return emptyList();
        }
    }

    public List<Statement> filterNewPbTransactions(ZonedDateTime start, ZonedDateTime end, List<Statement> pbStatements, User user) {
        final Comparator<ZonedDateTime> comparator = comparing(zdt -> zdt.truncatedTo(MILLIS));
        // sometimes new transactions can be available with delay, so we need to change start time of filtering
        final ZonedDateTime searchStartTime = start.minusMinutes(customConfig.getFilterTime());
        return pbStatements
                .stream()
                .filter(t -> {
                    final ZonedDateTime tTime = dateService.xmlDateTimeToZoned(t.getTrandate(), t.getTrantime(), user.getTimeZone());
                    return comparator.compare(searchStartTime, tTime) <= 0 && comparator.compare(end, tTime) > 0;
                }).collect(toList());
    }
}
