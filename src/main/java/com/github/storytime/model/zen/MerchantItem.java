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
public class MerchantItem {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private Object user;

    @JsonProperty("changed")
    private int changed;

    @Override
    public String toString() {
        return
                "MerchantItem{" +
                        "id = '" + id + '\'' +
                        ",title = '" + title + '\'' +
                        ",user = '" + user + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}