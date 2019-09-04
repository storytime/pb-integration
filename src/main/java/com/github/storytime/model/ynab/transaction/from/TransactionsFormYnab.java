package com.github.storytime.model.ynab.transaction.from;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class TransactionsFormYnab {

    @JsonProperty("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return
                "TransactionsFormYnab{" +
                        "data = '" + data + '\'' +
                        "}";
    }
}