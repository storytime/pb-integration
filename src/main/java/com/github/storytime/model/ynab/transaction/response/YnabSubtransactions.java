package com.github.storytime.model.ynab.transaction.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YnabSubtransactions {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("deleted")
    private boolean deleted;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("id")
    private String id;

    @JsonProperty("payee_id")
    private String payeeId;

    @JsonProperty("transfer_account_id")
    private String transferAccountId;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return
                "YnabSubtransactions{" +
                        "transaction_id = '" + transactionId + '\'' +
                        ",amount = '" + amount + '\'' +
                        ",deleted = '" + deleted + '\'' +
                        ",category_id = '" + categoryId + '\'' +
                        ",memo = '" + memo + '\'' +
                        ",id = '" + id + '\'' +
                        ",payee_id = '" + payeeId + '\'' +
                        ",transfer_account_id = '" + transferAccountId + '\'' +
                        "}";
    }
}