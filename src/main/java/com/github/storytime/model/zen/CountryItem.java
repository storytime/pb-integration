package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class CountryItem {

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("currency")
    private int currency;

    @JsonProperty("id")
    private int id;

    @JsonProperty("title")
    private String title;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return
                "CountryItem{" +
                        "domain = '" + domain + '\'' +
                        ",currency = '" + currency + '\'' +
                        ",id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        "}";
    }
}