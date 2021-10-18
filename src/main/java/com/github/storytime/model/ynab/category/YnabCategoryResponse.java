package com.github.storytime.model.ynab.category;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabCategoryResponse {

    @JsonProperty("data")
    private YnabCategoryData ynabCategoryData;

    public YnabCategoryData getYnabCategoryData() {
        return ynabCategoryData;
    }

    public void setYnabCategoryData(YnabCategoryData ynabCategoryData) {
        this.ynabCategoryData = ynabCategoryData;
    }

    @Override
    public String toString() {
        return
                "YnabCategoryResponse{" +
                        "ynabCategoryData = '" + ynabCategoryData + '\'' +
                        "}";
    }
}