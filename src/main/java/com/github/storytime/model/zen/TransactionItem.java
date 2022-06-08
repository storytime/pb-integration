package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public String toString() {
        return new StringJoiner(" ")
                .add("date = " + date)
                .add("income = " + income)
                .add("outcome = " + outcome)
                .add("opayee = " + originalPayee)
                .add("payee = " + payee)
                .add("merchant = " + merchant)
                .add("\n")
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