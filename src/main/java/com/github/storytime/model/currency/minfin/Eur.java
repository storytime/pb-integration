package com.github.storytime.model.currency.minfin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Eur {

    @JsonProperty("date")
    private String date;

    @JsonProperty("trendBid")
    private String trendBid;

    @JsonProperty("ask")
    private String ask;

    @JsonProperty("trendAsk")
    private String trendAsk;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("bid")
    private String bid;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTrendBid() {
        return trendBid;
    }

    public void setTrendBid(String trendBid) {
        this.trendBid = trendBid;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    public String getTrendAsk() {
        return trendAsk;
    }

    public void setTrendAsk(String trendAsk) {
        this.trendAsk = trendAsk;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    @Override
    public String toString() {
        return
                "Eur{" +
                        "date = '" + date + '\'' +
                        ",trendBid = '" + trendBid + '\'' +
                        ",ask = '" + ask + '\'' +
                        ",trendAsk = '" + trendAsk + '\'' +
                        ",currency = '" + currency + '\'' +
                        ",bid = '" + bid + '\'' +
                        "}";
    }
}