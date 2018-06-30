package com.github.storytime.model.db;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class User extends BaseEntity {

    @NotNull
    private String zenAuthToken;

    @NotNull
    private String timeZone;

    public String getZenAuthToken() {
        return zenAuthToken;
    }

    public User setZenAuthToken(String zenAuthToken) {
        this.zenAuthToken = zenAuthToken;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public User setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }
}
