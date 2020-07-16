package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.storytime.other.Utils.createHeader;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.LogManager.getLogger;

@Service
public class ZenDiffHttpService {

    private static final Logger LOGGER = getLogger(ZenDiffHttpService.class);

    private final RestTemplate restTemplate;
    private final CustomConfig customConfig;

    @Autowired
    public ZenDiffHttpService(final RestTemplate restTemplate,
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

    public Optional<ZenResponse> getZenDiffByUser(final Supplier<HttpEntity<ZenSyncRequest>> function) {
        try {
            final HttpEntity<ZenSyncRequest> httpEntity = function.get();
            final StopWatch st = new StopWatch();
            st.start();
            final Optional<ZenResponse> body = ofNullable(restTemplate.postForEntity(customConfig.getZenDiffUrl(), httpEntity, ZenResponse.class).getBody());
            st.stop();
            LOGGER.debug("Zen diff was fetched time:[{}]", st.getTotalTimeSeconds());
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch zen diff, error:[{}]", e.getMessage());
            return empty();
        }
    }
}
