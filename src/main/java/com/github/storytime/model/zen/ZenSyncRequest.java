package com.github.storytime.model.zen;

import org.springframework.lang.Nullable;

import java.util.Set;

public class ZenSyncRequest {

    private Long currentClientTimestamp;

    private Long serverTimestamp;

    @Nullable
    private Set<String> forceFetch;

    public Long getCurrentClientTimestamp() {
        return currentClientTimestamp;
    }

    public ZenSyncRequest setCurrentClientTimestamp(Long currentClientTimestamp) {
        this.currentClientTimestamp = currentClientTimestamp;
        return this;
    }

    public Long getServerTimestamp() {
        return serverTimestamp;
    }

    public ZenSyncRequest setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
        return this;
    }

    public Set<String> getForceFetch() {
        return forceFetch;
    }

    public ZenSyncRequest setForceFetch(Set<String> forceFetch) {
        this.forceFetch = forceFetch;
        return this;
    }
}
