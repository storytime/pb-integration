package com.github.storytime.model.ynab.budget;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabBudgetResponse {

    @JsonProperty("data")
    private YnabBudgetData ynabBudgetData;

    public YnabBudgetData getYnabBudgetData() {
        return ynabBudgetData;
    }

    public void setYnabBudgetData(YnabBudgetData ynabBudgetData) {
        this.ynabBudgetData = ynabBudgetData;
    }

    @Override
    public String toString() {
        return
                "YnabBudgetResponse{" +
                        "data = '" + ynabBudgetData + '\'' +
                        "}";
    }
}