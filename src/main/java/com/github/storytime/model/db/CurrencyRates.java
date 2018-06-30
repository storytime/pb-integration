package com.github.storytime.model.db;

import com.github.storytime.model.db.inner.CurrencySource;
import com.github.storytime.model.db.inner.CurrencyType;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static javax.persistence.EnumType.STRING;

@Entity
public class CurrencyRates extends BaseEntity {

    @NotNull
    @Enumerated(STRING)
    private CurrencySource currencySource;

    @NotNull
    @Enumerated(STRING)
    private CurrencyType currencyType;

    @NotNull
    private BigDecimal sellRate;

    @NotNull
    private BigDecimal buyRate;

    @NotNull
    private Long date;

    public CurrencySource getCurrencySource() {
        return currencySource;
    }

    public CurrencyRates setCurrencySource(CurrencySource currencySource) {
        this.currencySource = currencySource;
        return this;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public CurrencyRates setCurrencyType(CurrencyType currencyType) {
        this.currencyType = currencyType;
        return this;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }

    public CurrencyRates setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
        return this;
    }

    public BigDecimal getBuyRate() {
        return buyRate;
    }

    public CurrencyRates setBuyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
        return this;
    }

    public Long getDate() {
        return date;
    }

    public CurrencyRates setDate(Long date) {
        this.date = date;
        return this;
    }
}
