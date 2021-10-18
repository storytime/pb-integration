package com.github.storytime.model.ynab.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabAccountData {

    @JsonProperty("server_knowledge")
    private int serverKnowledge;

    @JsonProperty("accounts")
    private List<YnabAccounts> accounts;

    public int getServerKnowledge() {
        return serverKnowledge;
    }

    public void setServerKnowledge(int serverKnowledge) {
        this.serverKnowledge = serverKnowledge;
    }

    public List<YnabAccounts> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<YnabAccounts> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return
                "YnabAccountData{" +
                        "server_knowledge = '" + serverKnowledge + '\'' +
                        ",accounts = '" + accounts + '\'' +
                        "}";
    }
}