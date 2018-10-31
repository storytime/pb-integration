package com.github.storytime.model;

import com.github.storytime.model.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;

import java.util.Objects;
import java.util.StringJoiner;

import static java.time.Instant.now;

public class ExpiredPbStatement {

    private long transactionItemTime;

    private Statement zenTransactionItem;

    public ExpiredPbStatement(Statement zenTransactionItem) {
        this.zenTransactionItem = zenTransactionItem;
        this.transactionItemTime = now().toEpochMilli();
    }

    public ExpiredPbStatement(final long transactionItemTime,
                              final Statement zenTransactionItem) {
        this.transactionItemTime = transactionItemTime;
        this.zenTransactionItem = zenTransactionItem;
    }

    public long getTransactionItemTime() {
        return transactionItemTime;
    }

    public Statement getZenTransactionItem() {
        return zenTransactionItem;
    }

    /*
        transactionItemTime need to be skipped during comparison
    */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpiredPbStatement)) return false;
        ExpiredPbStatement that = (ExpiredPbStatement) o;
        return Objects.equals(getZenTransactionItem(), that.getZenTransactionItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getZenTransactionItem());
    }

    @Override
    public String toString() {

        return new StringJoiner(" ")
                .add("transactionItemTime = " + transactionItemTime)
                .add(zenTransactionItem.toString())
                .toString();
    }
}
