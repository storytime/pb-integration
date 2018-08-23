package com.github.storytime.model;

import com.github.storytime.model.zen.TransactionItem;

import java.util.Objects;

import static java.time.Instant.now;

public class ExpiredTransactionItem {

    private long transactionItemTime;

    private TransactionItem zenTransactionItem;

    public ExpiredTransactionItem(TransactionItem zenTransactionItem) {
        this.zenTransactionItem = zenTransactionItem;
        this.transactionItemTime = now().getEpochSecond();
    }

    public long getTransactionItemTime() {
        return transactionItemTime;
    }

    public TransactionItem getZenTransactionItem() {
        return zenTransactionItem;
    }

    /*
        transactionItemTime need to be skipped during comparison
    */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpiredTransactionItem)) return false;
        ExpiredTransactionItem that = (ExpiredTransactionItem) o;
        return Objects.equals(getZenTransactionItem(), that.getZenTransactionItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getZenTransactionItem());
    }
}
