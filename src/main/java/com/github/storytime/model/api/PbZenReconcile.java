package com.github.storytime.model.api;

public class PbZenReconcile {
    private String accountName;
    private String bankAmount;
    private String zenAmount;
    private String diff;


    public PbZenReconcile(final String accountName,
                          final String bankAmount,
                          final String zenAmount,
                          final String diff) {
        this.accountName = accountName;
        this.bankAmount = bankAmount;
        this.zenAmount = zenAmount;
        this.diff = diff;
    }

    public String getAccountName() {
        return accountName;
    }

    public PbZenReconcile setAccountName(final String accountName) {
        this.accountName = accountName;
        return this;
    }

    public String getBankAmount() {
        return bankAmount;
    }

    public PbZenReconcile setBankAmount(final String bankAmount) {
        this.bankAmount = bankAmount;
        return this;
    }

    public String getZenAmount() {
        return zenAmount;
    }

    public PbZenReconcile setZenAmount(final String zenAmount) {
        this.zenAmount = zenAmount;
        return this;
    }

    public String getDiff() {
        return diff;
    }

    public PbZenReconcile setDiff(final String diff) {
        this.diff = diff;
        return this;
    }
}
