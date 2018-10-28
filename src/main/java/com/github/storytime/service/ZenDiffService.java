package com.github.storytime.service;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.config.props.Constants;
import com.github.storytime.model.db.User;
import com.github.storytime.model.zen.*;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.other.Utils.createHeader;
import static java.lang.String.valueOf;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.right;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class ZenDiffService {

    private static final long INITIAL_TIMESTAMP = 0L;
    private static final Logger LOGGER = getLogger(ZenDiffService.class);
    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;
    private final Set<String> zenSyncForceFetchItems;


    @Autowired
    public ZenDiffService(final RestTemplate restTemplate,
                          final Set<String> zenSyncForceFetchItems,
                          final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
        this.zenSyncForceFetchItems = zenSyncForceFetchItems;
    }

    public Optional<ZenResponse> pushToZen(User u, ZenDiffRequest request) {
        try {
            final HttpEntity<ZenDiffRequest> diffObject = new HttpEntity<>(request, createHeader(u.getZenAuthToken()));
            final StopWatch st = new StopWatch();
            st.start();
            final ResponseEntity<ZenResponse> zenResponseResponseEntity = restTemplate
                    .postForEntity(customConfig.getZenDiffUrl(), diffObject, ZenResponse.class);
            st.stop();
            final Optional<ZenResponse> body = ofNullable(zenResponseResponseEntity.getBody());
            LOGGER.info("Updated zen diff was pushed to zen for user id: {} time: {}", u.getId(), st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot push Diff to ZEN request: {}", e.getMessage());
            return empty();
        }
    }

    public Optional<ZenResponse> getZenDiffByUser(final User u) {
        try {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond());

            if (u.getZenLastSyncTimestamp() == null || u.getZenLastSyncTimestamp() == INITIAL_TIMESTAMP) {
                zenSyncRequest.setForceFetch(null);
                zenSyncRequest.setServerTimestamp(INITIAL_TIMESTAMP);
            } else {
                zenSyncRequest.setForceFetch(zenSyncForceFetchItems);
                zenSyncRequest.setServerTimestamp(u.getZenLastSyncTimestamp());
            }

            final HttpEntity<ZenSyncRequest> request = new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ZenResponse> body = ofNullable(restTemplate.postForEntity(customConfig.getZenDiffUrl(), request, ZenResponse.class).getBody());
            st.stop();
            LOGGER.debug("Zen diff was fetched for u: {} last zen diff time: {}, time: {}", u.getId(), zenSyncRequest.getServerTimestamp(), st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch zen diff for user: {} : {}", u.getId(), e.getMessage());
            return empty();
        }
    }

    public String findAccountIdByPbCard(ZenResponse zenDiff, Long card) {
        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return zenDiff.getAccount()
                .stream()
                .filter(a -> ofNullable(a.getSyncID()).orElse(emptyList()).contains(carLastDigits))
                .findFirst()
                .map(AccountItem::getId)
                .orElse(EMPTY);
    }

    public Integer findCurrencyIdByShortLetter(ZenResponse zenDiff, String shortLetter) {
        return zenDiff.getInstrument()
                .stream()
                .filter(i -> i.getShortTitle().equalsIgnoreCase(shortLetter))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }


    public Optional<AccountItem> isCashAccountInCurrencyExists(ZenResponse zenDiff, String shortLetter) {
        final Integer id = findCurrencyIdByShortLetter(zenDiff, shortLetter);
        return zenDiff.getAccount()
                .stream()
                .filter(a -> a.getType().equalsIgnoreCase(Constants.CASH) && a.getInstrument() == id)
                .findFirst();
    }


    public Optional<String> findAccountIdByTwoCardDigits(final ZenResponse zenDiff,
                                                         final String lastTwoDigits,
                                                         final Long card) {

        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return zenDiff.getAccount()
                .stream()
                .filter(a -> !ofNullable(a.getSyncID()).orElse(emptyList()).contains(carLastDigits))
                .filter(a -> ofNullable(a.getSyncID()).orElse(emptyList())
                        .stream().anyMatch(s -> right(valueOf(s), CARD_TWO_DIGITS).equalsIgnoreCase(lastTwoDigits)))
                .findFirst()
                .map(AccountItem::getId);
    }
}
