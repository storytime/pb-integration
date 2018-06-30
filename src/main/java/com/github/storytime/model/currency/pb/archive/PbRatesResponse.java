package com.github.storytime.model.currency.pb.archive;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PbRatesResponse {

    @JsonProperty("date")
    private String date;

    @JsonProperty("bank")
    private String bank;

    @JsonProperty("exchangeRate")
    private List<ExchangeRateItem> exchangeRate;

    @JsonProperty("baseCurrency")
    private int baseCurrency;

    @JsonProperty("baseCurrencyLit")
    private String baseCurrencyLit;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public List<ExchangeRateItem> getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(List<ExchangeRateItem> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public int getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(int baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getBaseCurrencyLit() {
        return baseCurrencyLit;
    }

    public void setBaseCurrencyLit(String baseCurrencyLit) {
        this.baseCurrencyLit = baseCurrencyLit;
    }

    @Override
    public String toString() {
        return
                "PbRatesResponse{" +
                        "date = '" + date + '\'' +
                        ",bank = '" + bank + '\'' +
                        ",exchangeRate = '" + exchangeRate + '\'' +
                        ",baseCurrency = '" + baseCurrency + '\'' +
                        ",baseCurrencyLit = '" + baseCurrencyLit + '\'' +
                        "}";
    }
}