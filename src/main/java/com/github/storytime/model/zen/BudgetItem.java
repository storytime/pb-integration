package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class BudgetItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeLock")
    private boolean outcomeLock;

    @JsonProperty("incomeLock")
    private boolean incomeLock;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("outcome")
    private int outcome;

    @JsonProperty("changed")
    private int changed;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public boolean isOutcomeLock() {
        return outcomeLock;
    }

    public void setOutcomeLock(boolean outcomeLock) {
        this.outcomeLock = outcomeLock;
    }

    public boolean isIncomeLock() {
        return incomeLock;
    }

    public void setIncomeLock(boolean incomeLock) {
        this.incomeLock = incomeLock;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getOutcome() {
        return outcome;
    }

    public void setOutcome(int outcome) {
        this.outcome = outcome;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }

    @Override
    public String toString() {
        return
                "BudgetItem{" +
                        "date = '" + date + '\'' +
                        ",income = '" + income + '\'' +
                        ",outcomeLock = '" + outcomeLock + '\'' +
                        ",incomeLock = '" + incomeLock + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}