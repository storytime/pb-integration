package com.github.storytime.model.ynab.transaction.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabTransactions {

    @JsonProperty("date")
    private String date;

    @JsonProperty("transfer_transaction_id")
    private String transferTransactionId;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("import_id")
    private String importId;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("flag_color")
    private String flagColor;

    @JsonProperty("payee_id")
    private String payeeId;

    @JsonProperty("transfer_account_id")
    private String transferAccountId;

    @JsonProperty("approved")
    private boolean approved;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("payee_name")
    private String payeeName;

    @JsonProperty("subtransactions")
    private List<YnabSubtransactions> subtransactions;

    @JsonProperty("id")
    private String id;

    @JsonProperty("cleared")
    private String cleared;

    @JsonProperty("matched_transaction_id")
    private String matchedTransactionId;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTransferTransactionId() {
        return transferTransactionId;
    }

    public void setTransferTransactionId(String transferTransactionId) {
        this.transferTransactionId = transferTransactionId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
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

    public String getTransferAccountId() {
        return transferAccountId;
    }

    public void setTransferAccountId(String transferAccountId) {
        this.transferAccountId = transferAccountId;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public List<YnabSubtransactions> getSubtransactions() {
        return subtransactions;
    }

    public void setSubtransactions(List<YnabSubtransactions> subtransactions) {
        this.subtransactions = subtransactions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCleared() {
        return cleared;
    }

    public void setCleared(String cleared) {
        this.cleared = cleared;
    }

    public String getMatchedTransactionId() {
        return matchedTransactionId;
    }

    public void setMatchedTransactionId(String matchedTransactionId) {
        this.matchedTransactionId = matchedTransactionId;
    }

    @Override
    public String toString() {
        return
                "YnabTransactions{" +
                        "date = '" + date + '\'' +
                        ",transfer_transaction_id = '" + transferTransactionId + '\'' +
                        ",amount = '" + amount + '\'' +
                        ",category_name = '" + categoryName + '\'' +
                        ",import_id = '" + importId + '\'' +
                        ",memo = '" + memo + '\'' +
                        ",flag_color = '" + flagColor + '\'' +
                        ",payee_id = '" + payeeId + '\'' +
                        ",transfer_account_id = '" + transferAccountId + '\'' +
                        ",approved = '" + approved + '\'' +
                        ",account_id = '" + accountId + '\'' +
                        ",deleted = '" + deleted + '\'' +
                        ",category_id = '" + categoryId + '\'' +
                        ",account_name = '" + accountName + '\'' +
                        ",payee_name = '" + payeeName + '\'' +
                        ",subtransactions = '" + subtransactions + '\'' +
                        ",id = '" + id + '\'' +
                        ",cleared = '" + cleared + '\'' +
                        ",matched_transaction_id = '" + matchedTransactionId + '\'' +
                        "}";
    }
}