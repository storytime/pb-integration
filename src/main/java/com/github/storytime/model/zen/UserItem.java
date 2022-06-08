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