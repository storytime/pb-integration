package com.github.storytime.model.ynab.transaction.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;

@Generated("com.robohorse.robopojogenerator")
public class YnabTransactions {

    @NotNull
    @JsonProperty("date")
    private String date;

    @NotNull
    @JsonProperty("amount")
    private int amount;

    @JsonProperty("approved")
    private boolean approved;

    @NotNull
    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("import_id")
    private String importId;

    @JsonProperty("payee_name")
    private String payeeName;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("flag_color")
    private String flagColor;

    @JsonProperty("payee_id")
    private String payeeId;

    @JsonProperty("cleared")
    private String cleared;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getFlagColor() {
        return flagColor;
    }

    public void setFlagColor(String flagColor) {
        this.flagColor = flagColor;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getCleared() {
        return cleared;
    }

    public void setCleared(String cleared) {
        this.cleared = cleared;
    }

    @Override
    public String toString() {
        return
                "YnabTransactions{" +
                        "date = '" + date + '\'' +
                        ",amount = '" + amount + '\'' +
                        ",approved = '" + approved + '\'' +
                        ",account_id = '" + accountId + '\'' +
                        ",category_id = '" + categoryId + '\'' +
                        ",import_id = '" + importId + '\'' +
                        ",payee_name = '" + payeeName + '\'' +
                        ",memo = '" + memo + '\'' +
                        ",flag_color = '" + flagColor + '\'' +
                        ",payee_id = '" + payeeId + '\'' +
                        ",cleared = '" + cleared + '\'' +
                        "}";
    }
}