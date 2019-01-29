package com.github.storytime.model.api;

import java.math.BigDecimal;

public class SavingsInfo {

    private BigDecimal balance;
    private String currencySymbol;
    private BigDecimal inUah;
    private String title;
    private BigDecimal percent;

    public BigDecimal getBalance() {
        return balance;
    }

    public SavingsInfo setBalance(BigDecimal balance) {
        this.balance = balance;
        return this;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public SavingsInfo setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        return this;
    }

    public BigDecimal getInUah() {
        return inUah;
    }

    public SavingsInfo setInUah(BigDecimal inUah) {
        this.inUah = inUah;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SavingsInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public SavingsInfo setPercent(BigDecimal percent) {
        this.percent = percent;
        return this;
    }

}
