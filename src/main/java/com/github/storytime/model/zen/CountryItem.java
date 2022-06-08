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
public class CountryItem {

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("currency")
    private int currency;

    @JsonProperty("id")
    private int id;

    @JsonProperty("title")
    private String title;

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