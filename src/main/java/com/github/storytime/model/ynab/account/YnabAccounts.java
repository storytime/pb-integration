package com.github.storytime.model.ynab.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabAccounts {

    @JsonProperty("uncleared_balance")
    private int unclearedBalance;

    @JsonProperty("note")
    private Object note;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("balance")
    private int balance;

    @JsonProperty("name")
    private String name;

    @JsonProperty("cleared_balance")
    private int clearedBalance;

    @JsonProperty("transfer_payee_id")
    private String transferPayeeId;

    @JsonProperty("closed")
    private boolean closed;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("on_budget")
    private boolean onBudget;

    public int getUnclearedBalance() {
        return unclearedBalance;
    }

    public void setUnclearedBalance(int unclearedBalance) {
        this.unclearedBalance = unclearedBalance;
    }

    public Object getNote() {
        return note;
    }

    public void setNote(Object note) {
        this.note = note;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClearedBalance() {
        return clearedBalance;
    }

    public void setClearedBalance(int clearedBalance) {
        this.clearedBalance = clearedBalance;
    }

    public String getTransferPayeeId() {
        return transferPayeeId;
    }

    public void setTransferPayeeId(String transferPayeeId) {
        this.transferPayeeId = transferPayeeId;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOnBudget() {
        return onBudget;
    }

    public void setOnBudget(boolean onBudget) {
        this.onBudget = onBudget;
    }

    @Override
    public String toString() {
        return
                "YnabAccounts{" +
                        "uncleared_balance = '" + unclearedBalance + '\'' +
                        ",note = '" + note + '\'' +
                        ",deleted = '" + deleted + '\'' +
                        ",balance = '" + balance + '\'' +
                        ",name = '" + name + '\'' +
                        ",cleared_balance = '" + clearedBalance + '\'' +
                        ",transfer_payee_id = '" + transferPayeeId + '\'' +
                        ",closed = '" + closed + '\'' +
                        ",id = '" + id + '\'' +
                        ",type = '" + type + '\'' +
                        ",on_budget = '" + onBudget + '\'' +
                        "}";
    }
}