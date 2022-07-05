package com.github.storytime.function;

import com.github.storytime.model.aws.AppUser;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

import static com.github.storytime.config.props.ZenDataConstants.*;
import static com.github.storytime.other.Utils.createHeader;
import static java.time.Instant.now;
import static java.util.Set.of;

@Component
public class ZenDiffLambdaHolder {


    public Supplier<HttpEntity<ZenSyncRequest>> getInitialData(final AppUser user) {
        final long zenLastSyncTimestamp = user.getZenLastSyncTimestamp();
        if (zenLastSyncTimestamp == INITIAL_TIMESTAMP) {
            return this.requestToZenFunction(user, now().getEpochSecond(), INITIAL_TIMESTAMP, null);
        } else {
            return this.requestToZenFunction(user, now().getEpochSecond(), zenLastSyncTimestamp, of(USER, INSTRUMENT, ACCOUNT, MERCHANT));
        }
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getDataForSavings(final AppUser user) {
        return this.requestToZenFunction(user, now().getEpochSecond(), now().getEpochSecond(), of(ACCOUNT, INSTRUMENT));
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getDataForAccountAndTags(final AppUser user, long startDate) {
        return this.requestToZenFunction(user, now().getEpochSecond(), startDate, of(TAG, ACCOUNT));
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getDataForAccount(final AppUser user, long startDate) {
        return this.requestToZenFunction(user, now().getEpochSecond(), startDate, of(ACCOUNT));
    }

    public Supplier<HttpEntity<ZenSyncRequest>> requestToZenFunction(final AppUser user, long client,
                                                                     long startDate, final Set<String> forceFetch) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = ZenSyncRequest.builder()
                    .currentClientTimestamp(client)
                    .serverTimestamp(startDate)
                    .forceFetch(forceFetch)
                    .build();
            return new HttpEntity<>(zenSyncRequest, createHeader(user.getZenAuthToken()));
        };
    }
}
