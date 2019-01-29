package com.github.storytime.function;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

import static com.github.storytime.other.Utils.createHeader;
import static java.time.Instant.now;

@Component
public class ZenDiffLambdaHolder {

    private static final long INITIAL_TIMESTAMP = 0L;
    private static final String ACCOUNT = "account";
    private static final String INSTRUMENT = "instrument";
    private final Set<String> zenSyncForceFetchItems;

    @Autowired
    public ZenDiffLambdaHolder(final Set<String> zenSyncForceFetchItems) {
        this.zenSyncForceFetchItems = zenSyncForceFetchItems;
    }

    public Supplier<HttpEntity> getInitialFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(now().getEpochSecond());

            if (u.getZenLastSyncTimestamp() == null || u.getZenLastSyncTimestamp() == INITIAL_TIMESTAMP) {
                //fetch all data
                zenSyncRequest.setForceFetch(null);
                zenSyncRequest.setServerTimestamp(INITIAL_TIMESTAMP);
            } else {
                zenSyncRequest.setForceFetch(zenSyncForceFetchItems);
                zenSyncRequest.setServerTimestamp(u.getZenLastSyncTimestamp());
            }

            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity> getSavingsFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(now().getEpochSecond())
                    .setForceFetch(Set.of(ACCOUNT, INSTRUMENT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

}
