package com.github.storytime.model.currency.minfin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rub {

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

    @Override
    public String toString() {
        return
                "Rub{" +
                        "date = '" + date + '\'' +
                        ",trendBid = '" + trendBid + '\'' +
                        ",ask = '" + ask + '\'' +
                        ",trendAsk = '" + trendAsk + '\'' +
                        ",currency = '" + currency + '\'' +
                        ",bid = '" + bid + '\'' +
                        "}";
    }
}