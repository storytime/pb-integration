package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AccountItem {

    @JsonProperty("private")
    private boolean jsonMemberPrivate;

    @JsonProperty("role")
    private Object role;

    @JsonProperty("payoffInterval")
    private Object payoffInterval;

    @JsonProperty("instrument")
    private int instrument;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("percent")
    private Object percent;

    @JsonProperty("enableSMS")
    private boolean enableSMS;

    @JsonProperty("balance")
    private double balance;

    @JsonProperty("payoffStep")
    private Object payoffStep;

    @JsonProperty("creditLimit")
    private double creditLimit;

    @JsonProperty("company")
    private Integer company;

    @JsonProperty("endDateOffset")
    private Object endDateOffset;

    @JsonProperty("id")
    private String id;

    @JsonProperty("savings")
    private Boolean savings;

    @JsonProperty("startBalance")
    private double startBalance;

    @JsonProperty("inBalance")
    private boolean inBalance;

    @JsonProperty("enableCorrection")
    private boolean enableCorrection;

    @JsonProperty("archive")
    private boolean archive;

    @JsonProperty("syncID")
    private List<String> syncID;

    @JsonProperty("capitalization")
    private Object capitalization;

    @JsonProperty("endDateOffsetInterval")
    private Object endDateOffsetInterval;

    @JsonProperty("user")
    private int user;

    @JsonProperty("startDate")
    private Object startDate;

    @JsonProperty("changed")
    private long changed;


    public boolean isJsonMemberPrivate() {
        return jsonMemberPrivate;
    }

    public AccountItem setJsonMemberPrivate(boolean jsonMemberPrivate) {
        this.jsonMemberPrivate = jsonMemberPrivate;
        return this;
    }

    public Object getRole() {
        return role;
    }

    public AccountItem setRole(Object role) {
        this.role = role;
        return this;
    }

    public Object getPayoffInterval() {
        return payoffInterval;
    }

    public AccountItem setPayoffInterval(Object payoffInterval) {
        this.payoffInterval = payoffInterval;
        return this;
    }

    public int getInstrument() {
        return instrument;
    }

    public AccountItem setInstrument(int instrument) {
        this.instrument = instrument;
        return this;
    }

    public String getType() {
        return type;
    }

    public AccountItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AccountItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public Object getPercent() {
        return percent;
    }

    public AccountItem setPercent(Object percent) {
        this.percent = percent;
        return this;
    }

    public boolean isEnableSMS() {
        return enableSMS;
    }

    public AccountItem setEnableSMS(boolean enableSMS) {
        this.enableSMS = enableSMS;
        return this;
    }

    public double getBalance() {
        return balance;
    }

    public AccountItem setBalance(double balance) {
        this.balance = balance;
        return this;
    }

    public Object getPayoffStep() {
        return payoffStep;
    }

    public AccountItem setPayoffStep(Object payoffStep) {
        this.payoffStep = payoffStep;
        return this;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public AccountItem setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
        return this;
    }

    public Object getCompany() {
        return company;
    }

    public AccountItem setCompany(Integer company) {
        this.company = company;
        return this;
    }

    public Object getEndDateOffset() {
        return endDateOffset;
    }

    public AccountItem setEndDateOffset(Object endDateOffset) {
        this.endDateOffset = endDateOffset;
        return this;
    }

    public String getId() {
        return id;
    }

    public AccountItem setId(String id) {
        this.id = id;
        return this;
    }

    public Boolean getSavings() {
        return savings;
    }

    public AccountItem setSavings(Boolean savings) {
        this.savings = savings;
        return this;
    }

    public double getStartBalance() {
        return startBalance;
    }

    public AccountItem setStartBalance(double startBalance) {
        this.startBalance = startBalance;
        return this;
    }

    public boolean isInBalance() {
        return inBalance;
    }

    public AccountItem setInBalance(boolean inBalance) {
        this.inBalance = inBalance;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    public AccountItem setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }

    public boolean isArchive() {
        return archive;
    }

    public AccountItem setArchive(boolean archive) {
        this.archive = archive;
        return this;
    }

    public List<String> getSyncID() {
        return syncID;
    }

    public AccountItem setSyncID(List<String> syncID) {
        this.syncID = syncID;
        return this;
    }

    public Object getCapitalization() {
        return capitalization;
    }

    public AccountItem setCapitalization(Object capitalization) {
        this.capitalization = capitalization;
        return this;
    }

    public Object getEndDateOffsetInterval() {
        return endDateOffsetInterval;
    }

    public AccountItem setEndDateOffsetInterval(Object endDateOffsetInterval) {
        this.endDateOffsetInterval = endDateOffsetInterval;
        return this;
    }

    public int getUser() {
        return user;
    }

    public AccountItem setUser(int user) {
        this.user = user;
        return this;
    }

    public Object getStartDate() {
        return startDate;
    }

    public AccountItem setStartDate(Object startDate) {
        this.startDate = startDate;
        return this;
    }

    public long getChanged() {
        return changed;
    }

    public AccountItem setChanged(long changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public String toString() {
        return
                "AccountItem{" +
                        "private = '" + jsonMemberPrivate + '\'' +
                        ",role = '" + role + '\'' +
                        ",payoffInterval = '" + payoffInterval + '\'' +
                        ",instrument = '" + instrument + '\'' +
                        ",type = '" + type + '\'' +
                        ",title = '" + title + '\'' +
                        ",percent = '" + percent + '\'' +
                        ",enableSMS = '" + enableSMS + '\'' +
                        ",balance = '" + balance + '\'' +
                        ",payoffStep = '" + payoffStep + '\'' +
                        ",creditLimit = '" + creditLimit + '\'' +
                        ",company = '" + company + '\'' +
                        ",endDateOffset = '" + endDateOffset + '\'' +
                        ",id = '" + id + '\'' +
                        ",savings = '" + savings + '\'' +
                        ",startBalance = '" + startBalance + '\'' +
                        ",inBalance = '" + inBalance + '\'' +
                        ",enableCorrection = '" + enableCorrection + '\'' +
                        ",archive = '" + archive + '\'' +
                        ",syncID = '" + syncID + '\'' +
                        ",capitalization = '" + capitalization + '\'' +
                        ",endDateOffsetInterval = '" + endDateOffsetInterval + '\'' +
                        ",user = '" + user + '\'' +
                        ",startDate = '" + startDate + '\'' +
                        ",changed = '" + changed + '\'' +
                        "}";
    }
}