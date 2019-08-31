package com.github.storytime.model.internal;

import java.math.BigDecimal;

public class PbAccountBalance {

    private String account;
    private BigDecimal balance;

    public PbAccountBalance(String account, BigDecimal balance) {
        this.account = account;
        this.balance = balance;
    }

    public String getAccount() {
        return account;
    }

    public PbAccountBalance setAccount(String account) {
        this.account = account;
        return this;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public PbAccountBalance setBalance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }
}
