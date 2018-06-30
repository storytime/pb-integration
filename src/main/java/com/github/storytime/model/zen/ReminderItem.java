package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class ReminderItem {

    @JsonProperty("income")
    private int income;

    @JsonProperty("outcomeAccount")
    private String outcomeAccount;

    @JsonProperty("endDate")
    private Object endDate;

    @JsonProperty("incomeAccount")
    private String incomeAccount;

    @JsonProperty("merchant")
    private Object merchant;

    @JsonProperty("notify")
    private boolean notify;

    @JsonProperty("points")
    private List<Integer> points;

    @JsonProperty("payee")
    private Object payee;

    @JsonProperty("outcomeInstrument")
    private int outcomeInstrument;

    @JsonProperty("step")
    private int step;

    @JsonProperty("interval")
    private Object interval;

    @JsonProperty("comment")
    private Object comment;

    @JsonProperty("id")
    private String id;

    @JsonProperty("tag")
    private List<String> tag;

    @JsonProperty("user")
    private int user;

    @JsonProperty("incomeInstrument")
    private int incomeInstrument;

    @JsonProperty("outcome")
    private int outcome;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("changed")
    private int changed;

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

    public Object getEndDate() {
        return endDate;
    }

    public void setEndDate(Object endDate) {
        this.endDate = endDate;
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

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
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

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Object getInterval() {
        return interval;
    }

    public void setInterval(Object interval) {
        this.interval = interval;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
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
                "ReminderItem{" +
                        "income = '" + income + '\'' +
                        ",outcomeAccount = '" + outcomeAccount + '\'' +
                        ",endDate = '" + endDate + '\'' +
                        ",incomeAccount = '" + incomeAccount + '\'' +
                        ",merchant = '" + merchant + '\'' +
                        ",notify = '" + notify + '\'' +
                        ",points = '" + points + '\'' +
                        ",payee = '" + payee + '\'' +
                        ",outcomeInstrument = '" + outcomeInstrument + '\'' +
                        ",step = '" + step + '\'' +
                        ",interval = '" + interval + '\'' +
                        ",comment = '" + comment + '\'' +
                        ",id = '" + id + '\'' +
                        ",tag = '" + tag + '\'' +
                        ",user = '" + user + '\'' +
                        ",incomeInstrument = '" + incomeInstrument + '\'' +
                        ",outcome = '" + outcome + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}