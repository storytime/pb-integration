package com.github.storytime.function;

import com.github.storytime.config.props.Constants;
import com.github.storytime.model.api.ms.AppUser;
import com.github.storytime.model.db.YnabSyncConfig;
import com.github.storytime.model.zen.ZenSyncRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

import static com.github.storytime.other.Utils.createHeader;
import static java.time.Instant.now;
import static java.util.Set.of;

@Component
public class ZenDiffLambdaHolder {

    private final Set<String> zenSyncForceFetchItems;

    @Autowired
    public ZenDiffLambdaHolder(final Set<String> zenSyncForceFetchItems) {
        //todo better to hold here or in holder class
        this.zenSyncForceFetchItems = zenSyncForceFetchItems;
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getInitialFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(now().getEpochSecond());
            final Long zenLastSyncTimestamp = u.getZenLastSyncTimestamp();

            if (zenLastSyncTimestamp == null || zenLastSyncTimestamp == Constants.INITIAL_TIMESTAMP) {
                //fetch all data
                zenSyncRequest.setForceFetch(null);
                zenSyncRequest.setServerTimestamp(Constants.INITIAL_TIMESTAMP);
            } else {
                zenSyncRequest.setForceFetch(zenSyncForceFetchItems);
                zenSyncRequest.setServerTimestamp(zenLastSyncTimestamp);
            }

            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getYnabFunction(final AppUser user,
                                                                final long clientSyncTime,
                                                                final YnabSyncConfig ynabSyncConfig) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest().setCurrentClientTimestamp(clientSyncTime);
            zenSyncRequest.setForceFetch(of(Constants.TAG, Constants.ACCOUNT));
            zenSyncRequest.setServerTimestamp(ynabSyncConfig.getLastSync());
            return new HttpEntity<>(zenSyncRequest, createHeader(user.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getSavingsFunction(final AppUser u) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(now().getEpochSecond())
                    .setForceFetch(of(Constants.ACCOUNT, Constants.INSTRUMENT)); //TODO Make it cachible
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getAccountAndTags(final AppUser u, long startDate) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(startDate)
                    .setForceFetch(of(Constants.TAG, Constants.ACCOUNT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

    public Supplier<HttpEntity<ZenSyncRequest>> getAccount(final AppUser u, long startDate) {
        return () -> {
            final ZenSyncRequest zenSyncRequest = new ZenSyncRequest()
                    .setCurrentClientTimestamp(now().getEpochSecond())
                    .setServerTimestamp(startDate)
                    .setForceFetch(of(Constants.ACCOUNT));
            return new HttpEntity<>(zenSyncRequest, createHeader(u.getZenAuthToken()));
        };
    }

}
