package com.github.storytime.model.internal;

import com.github.storytime.model.pb.jaxb.statement.response.ok.Response.Data.Info.Statements.Statement;

import java.util.Objects;
import java.util.StringJoiner;

import static java.time.Instant.now;

public class ExpiredPbStatement {

    private final long transactionItemTime;

    private final Statement zenTransactionItem;

    public ExpiredPbStatement(final Statement zenTransactionItem) {
        this.zenTransactionItem = zenTransactionItem;
        this.transactionItemTime = now().toEpochMilli();
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
    public boolean equals(final Object o) {
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
