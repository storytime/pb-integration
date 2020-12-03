package com.github.storytime.model.api.ms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown=true)
public class AppUser {

    private long Id;

    @NotEmpty
    private String zenAuthToken;

    @NotEmpty
    private String timeZone;

    private Long zenLastSyncTimestamp;

    private String ynabAuthToken;

    private Boolean ynabSyncEnabled;

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

    public String getYnabAuthToken() {
        return ynabAuthToken;
    }

    public AppUser setYnabAuthToken(String ynabAuthToken) {
        this.ynabAuthToken = ynabAuthToken;
        return this;
    }

    public Boolean getYnabSyncEnabled() {
        return ynabSyncEnabled;
    }

    public AppUser setYnabSyncEnabled(Boolean ynabSyncEnabled) {
        this.ynabSyncEnabled = ynabSyncEnabled;
        return this;
    }

    public long getId() {
        return Id;
    }

    public AppUser setId(long id) {
        Id = id;
        return this;
    }
}
