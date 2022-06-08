package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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