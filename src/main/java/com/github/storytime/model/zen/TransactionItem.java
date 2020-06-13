package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Generated("com.robohorse.robopojogenerator")
public class TransactionItem {

    @JsonProperty("date")
    private String date;

    @JsonProperty("income")
    private Double income;

    @JsonProperty("opIncome")
    private Double opIncome;

    @JsonProperty("originalPayee")
    private String originalPayee;

    @JsonProperty("opOutcome")
    private Double opOutcome;

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
    private List<String> tag;

    @JsonProperty("outcomeBankID")
    private String outcomeBankID;

    @JsonProperty("outcome")
    private Double outcome;

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

    @JsonProperty("viewed")
    private boolean viewed;

    public String getDate() {
        return date;
    }

    public TransactionItem setDate(String date) {
        this.date = date;
        return this;
    }

    public Double getIncome() {
        return income;
    }

    public TransactionItem setIncome(Double income) {
        this.income = income;
        return this;
    }

    public Double getOpIncome() {
        return opIncome;
    }

    public TransactionItem setOpIncome(Double opIncome) {
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

    public Double getOpOutcome() {
        return opOutcome;
    }

    public TransactionItem setOpOutcome(Double opOutcome) {
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

    public List<String> getTag() {
        return tag;
    }

    public TransactionItem setTag(List<String> tag) {
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

    public Double getOutcome() {
        return outcome;
    }

    public TransactionItem setOutcome(Double outcome) {
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

    public String getOutcomeAccount() {
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

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    @Override
    public String toString() {
        return new StringJoiner(" ")
                .add("date = " + date)
                .add("income = " + income)
                .add("outcome = " + outcome)
                .add("opayee = " + originalPayee)
                .add("payee = " + payee)
                .add("cr = " + created)
                .add("com = " + comment)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionItem)) return false;
        TransactionItem that = (TransactionItem) o;
        return getOpOutcome() == that.getOpOutcome() &&
                getUser() == that.getUser() &&
                Objects.equals(getDate(), that.getDate()) &&
                Objects.equals(getIncome(), that.getIncome()) &&
                Objects.equals(getOpIncome(), that.getOpIncome()) &&
                Objects.equals(getOriginalPayee(), that.getOriginalPayee()) &&
                Objects.equals(getOutcomeBankID(), that.getOutcomeBankID()) &&
                Objects.equals(getOutcome(), that.getOutcome()) &&
                Objects.equals(getOutcomeAccount(), that.getOutcomeAccount()) &&
                Objects.equals(getIncomeBankID(), that.getIncomeBankID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getIncome(), getOpIncome(), getOriginalPayee(), getOpOutcome(),
                getOutcomeBankID(), getOutcome(), getOutcomeAccount(), getIncomeBankID(), getUser());
    }
}