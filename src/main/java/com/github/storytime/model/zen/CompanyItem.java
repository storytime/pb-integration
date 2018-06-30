package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class CompanyItem {

    @JsonProperty("country")
    private int country;

    @JsonProperty("fullTitle")
    private Object fullTitle;

    @JsonProperty("www")
    private String www;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("id")
    private int id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("changed")
    private int changed;

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public Object getFullTitle() {
        return fullTitle;
    }

    public void setFullTitle(Object fullTitle) {
        this.fullTitle = fullTitle;
    }

    public String getWww() {
        return www;
    }

    public void setWww(String www) {
        this.www = www;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return
                "CompanyItem{" +
                        "country = '" + country + '\'' +
                        ",fullTitle = '" + fullTitle + '\'' +
                        ",www = '" + www + '\'' +
                        ",countryCode = '" + countryCode + '\'' +
                        ",id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}