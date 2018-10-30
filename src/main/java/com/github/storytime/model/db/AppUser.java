package com.github.storytime.model.db;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class AppUser extends BaseEntity {

    @NotNull
    private String zenAuthToken;

    @NotNull
    private String timeZone;

    @NotNull
    private Long zenLastSyncTimestamp;

    public String getZenAuthToken() {
        return zenAuthToken;
    }

    public AppUser setZenAuthToken(String zenAuthToken) {
        this.zenAuthToken = zenAuthToken;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public AppUser setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public Long getZenLastSyncTimestamp() {
        return zenLastSyncTimestamp;
    }

    public AppUser setZenLastSyncTimestamp(Long zenLastSyncTimestamp) {
        this.zenLastSyncTimestamp = zenLastSyncTimestamp;
        return this;
    }
}
