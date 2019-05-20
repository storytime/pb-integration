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

    private String ynabAuthToken;

    private Boolean ynabSyncEnabled;

    private Long ynabLastSyncTimestamp;

    private String ynabSyncBudget;

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

    public Long getYnabLastSyncTimestamp() {
        return ynabLastSyncTimestamp;
    }

    public AppUser setYnabLastSyncTimestamp(Long ynabLastSyncTimestamp) {
        this.ynabLastSyncTimestamp = ynabLastSyncTimestamp;
        return this;
    }

    public String getYnabSyncBudget() {
        return ynabSyncBudget;
    }

    public AppUser setYnabSyncBudget(String ynabSyncBudget) {
        this.ynabSyncBudget = ynabSyncBudget;
        return this;
    }
}
