package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class TransactionItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private Float income;

    @JsonProperty("opIncome")
    private Float opIncome;

    @JsonProperty("originalPayee")
    private String originalPayee;

    @JsonProperty("opOutcome")
    private int opOutcome;

    @JsonProperty("latitude")
    private Object latitude;

    @JsonProperty("hold")
    private boolean hold;

    @JsonProperty("payee")
    private String payee;

    @JsonProperty("qrCode")
    private Object qrCode;

    @JsonProperty("opIncomeInstrument")
    private Integer opIncomeInstrument;

    @JsonProperty("id")
    private String id;

    @JsonProperty("tag")
    private Object tag;

    @JsonProperty("outcomeBankID")
    private String outcomeBankID;

    @JsonProperty("outcome")
    private Float outcome;

    @JsonProperty("opOutcomeInstrument")
    private Object opOutcomeInstrument;

    @JsonProperty("longitude")
    private Object longitude;

    @JsonProperty("outcomeAccount")
    private String outcomeAccount;

    @JsonProperty("created")
    private long created;

    @JsonProperty("incomeAccount")
    private String incomeAccount;

    @JsonProperty("merchant")
    private String merchant;

    @JsonProperty("reminderMarker")
    private Object reminderMarker;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("incomeBankID")
    private String incomeBankID;

    @JsonProperty("outcomeInstrument")
    private int outcomeInstrument;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("user")
    private int user;

    @JsonProperty("incomeInstrument")
    private int incomeInstrument;

    @JsonProperty("changed")
    private int changed;

    public String getDate() {
        return date;
    }

    public TransactionItem setDate(String date) {
        this.date = date;
        return this;
    }

    public Float getIncome() {
        return income;
    }

    public TransactionItem setIncome(Float income) {
        this.income = income;
        return this;
    }

    public Float getOpIncome() {
        return opIncome;
    }

    public TransactionItem setOpIncome(Float opIncome) {
        this.opIncome = opIncome;
        return this;
    }

    public String getOriginalPayee() {
        return originalPayee;
    }

    public TransactionItem setOriginalPayee(String originalPayee) {
        this.originalPayee = originalPayee;
        return this;
    }

    public int getOpOutcome() {
        return opOutcome;
    }

    public TransactionItem setOpOutcome(int opOutcome) {
        this.opOutcome = opOutcome;
        return this;
    }

    public Object getLatitude() {
        return latitude;
    }

    public TransactionItem setLatitude(Object latitude) {
        this.latitude = latitude;
        return this;
    }

    public boolean isHold() {
        return hold;
    }

    public TransactionItem setHold(boolean hold) {
        this.hold = hold;
        return this;
    }

    public String getPayee() {
        return payee;
    }

    public TransactionItem setPayee(String payee) {
        this.payee = payee;
        return this;
    }

    public Object getQrCode() {
        return qrCode;
    }

    public TransactionItem setQrCode(Object qrCode) {
        this.qrCode = qrCode;
        return this;
    }

    public Object getOpIncomeInstrument() {
        return opIncomeInstrument;
    }

    public TransactionItem setOpIncomeInstrument(Integer opIncomeInstrument) {
        this.opIncomeInstrument = opIncomeInstrument;
        return this;
    }

    public String getId() {
        return id;
    }

    public TransactionItem setId(String id) {
        this.id = id;
        return this;
    }

    public Object getTag() {
        return tag;
    }

    public TransactionItem setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public Object getOutcomeBankID() {
        return outcomeBankID;
    }

    public TransactionItem setOutcomeBankID(String outcomeBankID) {
        this.outcomeBankID = outcomeBankID;
        return this;
    }

    public Float getOutcome() {
        return outcome;
    }

    public TransactionItem setOutcome(Float outcome) {
        this.outcome = outcome;
        return this;
    }

    public Object getOpOutcomeInstrument() {
        return opOutcomeInstrument;
    }

    public TransactionItem setOpOutcomeInstrument(Object opOutcomeInstrument) {
        this.opOutcomeInstrument = opOutcomeInstrument;
        return this;
    }

    public Object getLongitude() {
        return longitude;
    }

    public TransactionItem setLongitude(Object longitude) {
        this.longitude = longitude;
        return this;
    }

    public Object getOutcomeAccount() {
        return outcomeAccount;
    }

    public TransactionItem setOutcomeAccount(String outcomeAccount) {
        this.outcomeAccount = outcomeAccount;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public TransactionItem setCreated(long created) {
        this.created = created;
        return this;
    }

    public String getIncomeAccount() {
        return incomeAccount;
    }

    public TransactionItem setIncomeAccount(String incomeAccount) {
        this.incomeAccount = incomeAccount;
        return this;
    }

    public String getMerchant() {
        return merchant;
    }

    public TransactionItem setMerchant(String merchant) {
        this.merchant = merchant;
        return this;
    }

    public Object getReminderMarker() {
        return reminderMarker;
    }

    public TransactionItem setReminderMarker(Object reminderMarker) {
        this.reminderMarker = reminderMarker;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public TransactionItem setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Object getIncomeBankID() {
        return incomeBankID;
    }

    public TransactionItem setIncomeBankID(String incomeBankID) {
        this.incomeBankID = incomeBankID;
        return this;
    }

    public int getOutcomeInstrument() {
        return outcomeInstrument;
    }

    public TransactionItem setOutcomeInstrument(int outcomeInstrument) {
        this.outcomeInstrument = outcomeInstrument;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public TransactionItem setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public int getUser() {
        return user;
    }

    public TransactionItem setUser(int user) {
        this.user = user;
        return this;
    }

    public int getIncomeInstrument() {
        return incomeInstrument;
    }

    public TransactionItem setIncomeInstrument(int incomeInstrument) {
        this.incomeInstrument = incomeInstrument;
        return this;
    }

    public int getChanged() {
        return changed;
    }

    public TransactionItem setChanged(int changed) {
        this.changed = changed;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("date: ").append(date)
                .append("income: ").append(income)
                .append("outcome: ").append(outcome)
                .append("opayee: ").append(originalPayee)
                .append("payee: ").append(payee)
                .append("cr: ").append(created)
                .append("com: ").append(comment)
                .toString();
    }
}