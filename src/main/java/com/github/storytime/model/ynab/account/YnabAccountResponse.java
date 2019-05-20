package com.github.storytime.model.ynab.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YnabAccountResponse {

    @JsonProperty("data")
    private YnabAccountData ynabAccountData;

    public YnabAccountData getYnabAccountData() {
        return ynabAccountData;
    }

    public void setYnabAccountData(YnabAccountData ynabAccountData) {
        this.ynabAccountData = ynabAccountData;
    }

    @Override
    public String toString() {
        return
                "YnabAccountResponse{" +
                        "ynabAccountData = '" + ynabAccountData + '\'' +
                        "}";
    }
}