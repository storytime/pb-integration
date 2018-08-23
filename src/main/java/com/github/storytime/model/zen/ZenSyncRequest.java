package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ZenSyncRequest {

    private Long currentClientTimestamp;

    private Long serverTimestamp;


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
