package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class InstrumentItem {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("rate")
    private double rate;

    @JsonProperty("id")
    private int id;

    @JsonProperty("shortTitle")
    private String shortTitle;

    @JsonProperty("title")
    private String title;

    @JsonProperty("changed")
    private int changed;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortTitle() {
        return shortTitle;
    }

    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
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
                "InstrumentItem{" +
                        "symbol = '" + symbol + '\'' +
                        ",rate = '" + rate + '\'' +
                        ",id = '" + id + '\'' +
                        ",shortTitle = '" + shortTitle + '\'' +
                        ",title = '" + title + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}