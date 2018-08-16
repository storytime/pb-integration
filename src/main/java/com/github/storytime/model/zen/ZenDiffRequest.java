package com.github.storytime.model.zen;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZenDiffRequest {

    private Long currentClientTimestamp;
    private long lastServerTimestamp;
    private List<AccountItem> account;
    private List<TransactionItem> transaction;

    public List<TransactionItem> getTransaction() {
        return transaction;
    }

    public ZenDiffRequest setTransaction(List<TransactionItem> transaction) {
        this.transaction = transaction;
        return this;
    }

    public List<AccountItem> getAccount() {

        return account;
    }

    public ZenDiffRequest setAccount(List<AccountItem> account) {
        this.account = account;
        return this;
    }

    public Long getCurrentClientTimestamp() {
        return currentClientTimestamp;
    }

    public ZenDiffRequest setCurrentClientTimestamp(Long currentClientTimestamp) {
        this.currentClientTimestamp = currentClientTimestamp;
        return this;
    }

    public long getLastServerTimestamp() {
        return lastServerTimestamp;
    }

    public ZenDiffRequest setLastServerTimestamp(long lastServerTimestamp) {
        this.lastServerTimestamp = lastServerTimestamp;
        return this;
    }
}
