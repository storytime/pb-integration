package com.github.storytime.function;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.github.storytime.config.props.Constants.*;
import static com.github.storytime.other.Utils.createHeader;
import static java.time.Instant.now;
import static java.util.Set.of;

@Component
public class ZenDiffLambdaHolder {


    public Supplier<HttpEntity<ZenSyncRequest>> getInitialFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(now().getEpochSecond());
            final Long zenLastSyncTimestamp = u.getZenLastSyncTimestamp();

            if (zenLastSyncTimestamp == null || zenLastSyncTimestamp == INITIAL_TIMESTAMP) {
                //fetch all data
                zenSyncRequest.setForceFetch(null);
                zenSyncRequest.setServerTimestamp(INITIAL_TIMESTAMP);
            } else {
                zenSyncRequest.setForceFetch(of(USER, INSTRUMENT, ACCOUNT, MERCHANT));
                zenSyncRequest.setServerTimestamp(zenLastSyncTimestamp);
            }

            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getSavingsFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(now().getEpochSecond())
                    .setForceFetch(of(ACCOUNT, INSTRUMENT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getAccountAndTags(final AppUser u, long startDate) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(startDate)
                    .setForceFetch(of(TAG, ACCOUNT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getAccount(final AppUser u, long startDate) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(startDate)
                    .setForceFetch(of(ACCOUNT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }
}
