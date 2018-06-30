package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class ReminderMarkerItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeAccount")
    private String outcomeAccount;

    @JsonProperty("reminder")
    private String reminder;

    @JsonProperty("incomeAccount")
    private String incomeAccount;

    @JsonProperty("merchant")
    private Object merchant;

    @JsonProperty("notify")
    private boolean notify;

    @JsonProperty("payee")
    private Object payee;

    @JsonProperty("outcomeInstrument")
    private int outcomeInstrument;

    @JsonProperty("comment")
    private Object comment;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("tag")
    private List<String> tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("incomeInstrument")
    private int incomeInstrument;

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

    public String getOutcomeAccount() {
        return outcomeAccount;
    }

    public void setOutcomeAccount(String outcomeAccount) {
        this.outcomeAccount = outcomeAccount;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getIncomeAccount() {
        return incomeAccount;
    }

    public void setIncomeAccount(String incomeAccount) {
        this.incomeAccount = incomeAccount;
    }

    public Object getMerchant() {
        return merchant;
    }

    public void setMerchant(Object merchant) {
        this.merchant = merchant;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public Object getPayee() {
        return payee;
    }

    public void setPayee(Object payee) {
        this.payee = payee;
    }

    public int getOutcomeInstrument() {
        return outcomeInstrument;
    }

    public void setOutcomeInstrument(int outcomeInstrument) {
        this.outcomeInstrument = outcomeInstrument;
    }

    public Object getComment() {
        return comment;
    }

    public void setComment(Object comment) {
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getIncomeInstrument() {
        return incomeInstrument;
    }

    public void setIncomeInstrument(int incomeInstrument) {
        this.incomeInstrument = incomeInstrument;
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
                "ReminderMarkerItem{" +
                        "date = '" + date + '\'' +
                        ",income = '" + income + '\'' +
                        ",outcomeAccount = '" + outcomeAccount + '\'' +
                        ",reminder = '" + reminder + '\'' +
                        ",incomeAccount = '" + incomeAccount + '\'' +
                        ",merchant = '" + merchant + '\'' +
                        ",notify = '" + notify + '\'' +
                        ",payee = '" + payee + '\'' +
                        ",outcomeInstrument = '" + outcomeInstrument + '\'' +
                        ",comment = '" + comment + '\'' +
                        ",id = '" + id + '\'' +
                        ",state = '" + state + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",incomeInstrument = '" + incomeInstrument + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}