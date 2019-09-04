package com.github.storytime.model.ynab.transaction.from;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class Data {

    @JsonProperty("server_knowledge")
    private int serverKnowledge;

    @JsonProperty("transactions")
    private List<TransactionsItem> transactions;

    public int getServerKnowledge() {
        return serverKnowledge;
    }

    public void setServerKnowledge(int serverKnowledge) {
        this.serverKnowledge = serverKnowledge;
    }

    public List<TransactionsItem> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionsItem> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return
                "Data{" +
                        "server_knowledge = '" + serverKnowledge + '\'' +
                        ",transactions = '" + transactions + '\'' +
                        "}";
    }
}