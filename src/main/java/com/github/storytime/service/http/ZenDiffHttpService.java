package com.github.storytime.service.http;

import com.github.storytime.config.CustomConfig;
import com.github.storytime.model.aws.AwsUser;
import com.github.storytime.model.zen.ZenDiffRequest;
import com.github.storytime.model.zen.ZenResponse;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.function.Supplier;

import static com.github.storytime.STUtils.createSt;
import static com.github.storytime.STUtils.getTimeAndReset;
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

    public Optional<ZenResponse> pushToZen(final AwsUser u, final ZenDiffRequest request) {
        final var st = createSt();
        try {
            final var diffObject = new HttpEntity<>(request, createHeader(u.getZenAuthToken()));
            final var zenResponseResponseEntity = restTemplate.postForEntity(customConfig.getZenDiffUrl(), diffObject, ZenResponse.class);
            final var body = ofNullable(zenResponseResponseEntity.getBody());
            LOGGER.info("Finish! Updated zen diff with: [{}], was pushed to zen for user id: [{}], time: [{}] - finish", request.getTransaction(), u.getId(), getTimeAndReset(st));
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot push zen diff, time: [{}], request: [{}] - error", getTimeAndReset(st), e.getMessage());
            return empty();
        }
    }

    public Optional<ZenResponse> getZenDiffByUser(final Supplier<HttpEntity<ZenSyncRequest>> function) {
        final var st = createSt();
        try {
            final var httpEntity = function.get();
            final var body = ofNullable(restTemplate.postForEntity(customConfig.getZenDiffUrl(), httpEntity, ZenResponse.class).getBody());
            LOGGER.debug("Zen diff was fetched time: [{}] - finish!", getTimeAndReset(st));
            return body;
        } catch (Exception e) {
            LOGGER.error("Cannot fetch zen diff, time: [{}], error: [{}]", getTimeAndReset(st), e.getMessage());
            return empty();
        }
    }
}
