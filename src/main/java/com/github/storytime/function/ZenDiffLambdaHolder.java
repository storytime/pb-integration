package com.github.storytime.function;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
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
    private static final String TRANSACTION = "transaction";
    private static final String INSTRUMENT = "instrument";
    private static final String TAG = "tag";
    private final Set<String> zenSyncForceFetchItems;

    @Autowired
    public ZenDiffLambdaHolder(final Set<String> zenSyncForceFetchItems) {
        //todo better to hold here or in holder class
        this.zenSyncForceFetchItems = zenSyncForceFetchItems;
    }

    public Supplier<HttpEntity> getInitialFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(now().getEpochSecond());
            final Long zenLastSyncTimestamp = u.getZenLastSyncTimestamp();

            if (zenLastSyncTimestamp == null || zenLastSyncTimestamp == INITIAL_TIMESTAMP) {
                //fetch all data
                zenSyncRequest.setForceFetch(null);
                zenSyncRequest.setServerTimestamp(INITIAL_TIMESTAMP);
            } else {
                zenSyncRequest.setForceFetch(zenSyncForceFetchItems);
                zenSyncRequest.setServerTimestamp(zenLastSyncTimestamp);
            }

            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity> getYnabFunction(final AppUser user,
                                                final long clientSyncTime,
                                                final YnabSyncConfig ynabSyncConfig) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(clientSyncTime);
            zenSyncRequest.setForceFetch(Set.of(TAG, ACCOUNT));
            zenSyncRequest.setServerTimestamp(ynabSyncConfig.getLastSync());
            return new HttpEntity<>(zenSyncRequest, createHeader(user.getZenAuthToken()));
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

    public Supplier<HttpEntity> getAccount(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(now().getEpochSecond())
                    .setForceFetch(Set.of(TAG, ACCOUNT, TRANSACTION));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

}
