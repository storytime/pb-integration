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
public class TagItem {

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("color")
    private long color;

    @JsonProperty("budgetOutcome")
    private boolean budgetOutcome;

    @JsonProperty("showIncome")
    private boolean showIncome;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("title")
    private String title;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("picture")
    private Object picture;

    @JsonProperty("budgetIncome")
    private boolean budgetIncome;

    @JsonProperty("id")
    private String id;

    @JsonProperty("showOutcome")
    private boolean showOutcome;

    @JsonProperty("user")
    private int user;

    @JsonProperty("changed")
    private int changed;


    @Override
    public String toString() {
        return
                "TagItem{" +
                        "parent = '" + parent + '\'' +
                        ",color = '" + color + '\'' +
                        ",budgetOutcome = '" + budgetOutcome + '\'' +
                        ",showIncome = '" + showIncome + '\'' +
                        ",icon = '" + icon + '\'' +
                        ",title = '" + title + '\'' +
                        ",required = '" + required + '\'' +
                        ",picture = '" + picture + '\'' +
                        ",budgetIncome = '" + budgetIncome + '\'' +
                        ",id = '" + id + '\'' +
                        ",showOutcome = '" + showOutcome + '\'' +
                        ",user = '" + user + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}