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