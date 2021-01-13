package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class UserItem {

    @JsonProperty("country")
    private int country;

    @JsonProperty("parent")
    private Object parent;

    @JsonProperty("paidTill")
    private long paidTill;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("currency")
    private int currency;

    @JsonProperty("id")
    private int id;

    @JsonProperty("subscription")
    private String subscription;

    @JsonProperty("login")
    private String login;

    @JsonProperty("changed")
    private int changed;

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public long getPaidTill() {
        return paidTill;
    }

    public void setPaidTill(long paidTill) {
        this.paidTill = paidTill;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return
                "UserItem{" +
                        "country = '" + country + '\'' +
                        ",parent = '" + parent + '\'' +
                        ",paidTill = '" + paidTill + '\'' +
                        ",countryCode = '" + countryCode + '\'' +
                        ",currency = '" + currency + '\'' +
                        ",id = '" + id + '\'' +
                        ",subscription = '" + subscription + '\'' +
                        ",login = '" + login + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}