package com.github.storytime.model.ynab.transaction.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class TransactionPushResponse {

    @JsonProperty("data")
    private YnabTransactionData ynabTransactionData;

    public YnabTransactionData getYnabTransactionData() {
        return ynabTransactionData;
    }

    public void setYnabTransactionData(YnabTransactionData ynabTransactionData) {
        this.ynabTransactionData = ynabTransactionData;
    }

    @Override
    public String toString() {
        return
                "TransactionPushResponse{" +
                        "ynabTransactionData = '" + ynabTransactionData + '\'' +
                        "}";
    }
}