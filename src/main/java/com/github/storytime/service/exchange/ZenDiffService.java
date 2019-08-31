package com.github.storytime.service.exchange;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.zen.AccountItem;
import com.github.storytime.model.zen.InstrumentItem;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.other.Utils.createHeader;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.right;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class ZenDiffService {

    private static final Logger LOGGER = getLogger(ZenDiffService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;


    @Autowired
    public ZenDiffService(final RestTemplate restTemplate,
                          final CustomConfig customConfig) {
        this.restTemplate = restTemplate;
        this.customConfig = customConfig;
    }

    public Optional<ZenResponse> pushToZen(final AppUser u, final ZenDiffRequest request) {
        try {
            final HttpEntity<ZenDiffRequest> diffObject = new HttpEntity<>(request, createHeader(u.getZenAuthToken()));
            final StopWatch st = new StopWatch();
            st.start();
            final ResponseEntity<ZenResponse> zenResponseResponseEntity = restTemplate
                    .postForEntity(customConfig.getZenDiffUrl(), diffObject, ZenResponse.class);
            st.stop();
            final Optional<ZenResponse> body = ofNullable(zenResponseResponseEntity.getBody());
            LOGGER.info("Finish! Updated zen diff with [{}] was pushed to zen for user id:[{}] time:[{}]", request.getTransaction(), u.getId(), st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot push Diff to ZEN request:[{}]", e.getMessage());
            return empty();
        }
    }

    public Optional<ZenResponse> getZenDiffByUser(final Supplier<HttpEntity> function) {
        try {
            final HttpEntity httpEntity = function.get();
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ZenResponse> body = ofNullable(restTemplate.postForEntity(customConfig.getZenDiffUrl(), httpEntity, ZenResponse.class).getBody());
            st.stop();
            LOGGER.debug("Zen diff was fetched time:[{}]", st.getTotalTimeMillis());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch zen diff, error:[{}]", e.getMessage());
            return empty();
        }
    }

    public String findAccountIdByPbCard(final ZenResponse zenDiff, final Long card) {
        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return zenDiff.getAccount()
                .stream()
                .filter(a -> ofNullable(a.getSyncID()).orElse(emptyList()).contains(carLastDigits))
                .findFirst()
                .map(AccountItem::getId)
                .orElse(EMPTY);
    }

    public Integer findCurrencyIdByShortLetter(final ZenResponse zenDiff, final String shortLetter) {
        return zenDiff.getInstrument()
                .stream()
                .filter(i -> i.getShortTitle().equalsIgnoreCase(shortLetter))
                .findFirst()
                .map(InstrumentItem::getId)
                .orElse(DEFAULT_CURRENCY_ZEN);
    }

    public Optional<AccountItem> isCashAccountInCurrencyExists(final ZenResponse zenDiff, final String shortLetter) {
        final Integer id = findCurrencyIdByShortLetter(zenDiff, shortLetter);
        return zenDiff.getAccount()
                .stream()
                .filter(a -> a.getType().equalsIgnoreCase(CASH) && a.getInstrument() == id)
                .findFirst();
    }

    public Optional<String> findAccountIdByTwoCardDigits(final ZenResponse zenDiff,
                                                         final String lastTwoDigits,
                                                         final Long card) {

        final String carLastDigits = right(valueOf(card), CARD_LAST_DIGITS);
        return zenDiff.getAccount()
                .stream()
                .filter(not(a -> ofNullable(a.getSyncID())
                        .orElse(emptyList())
                        .contains(carLastDigits)))
                .filter(a -> ofNullable(a.getSyncID())
                        .orElse(emptyList())
                        .stream()
                        .anyMatch(s -> right(valueOf(s), CARD_TWO_DIGITS).equalsIgnoreCase(lastTwoDigits)))
                .findFirst()
                .map(AccountItem::getId);
    }
}
