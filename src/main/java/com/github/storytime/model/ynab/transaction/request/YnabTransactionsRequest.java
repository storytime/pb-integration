package com.github.storytime.model.ynab.transaction.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class YnabTransactionsRequest {

    @JsonProperty("transactions")
    private List<YnabTransactions> transactions;

    public List<YnabTransactions> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<YnabTransactions> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return
                "YnabTransactionsRequest{" +
                        "transactions = '" + transactions + '\'' +
                        "}";
    }
}