package com.github.storytime.model.zen;

public class InitialSyncRequest {

    private Long currentClientTimestamp;
    private Long serverTimestamp;

    public Long getCurrentClientTimestamp() {
        return currentClientTimestamp;
    }

    public InitialSyncRequest setCurrentClientTimestamp(Long currentClientTimestamp) {
        this.currentClientTimestamp = currentClientTimestamp;
        return this;
    }

    public Long getServerTimestamp() {
        return serverTimestamp;
    }

    public InitialSyncRequest setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
        return this;
    }
}
