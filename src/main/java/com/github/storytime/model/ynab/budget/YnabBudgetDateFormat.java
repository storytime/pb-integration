package com.github.storytime.model.ynab.budget;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YnabBudgetDateFormat {

    @JsonProperty("format")
    private String format;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return
                "YnabBudgetDateFormat{" +
                        "format = '" + format + '\'' +
                        "}";
    }
}