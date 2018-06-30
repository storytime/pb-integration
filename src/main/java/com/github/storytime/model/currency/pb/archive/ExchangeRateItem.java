package com.github.storytime.model.currency.pb.archive;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExchangeRateItem {

    @JsonProperty("saleRateNB")
    private double saleRateNB;

    @JsonProperty("purchaseRateNB")
    private double purchaseRateNB;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("baseCurrency")
    private String baseCurrency;

    public double getSaleRateNB() {
        return saleRateNB;
    }

    public void setSaleRateNB(double saleRateNB) {
        this.saleRateNB = saleRateNB;
    }

    public double getPurchaseRateNB() {
        return purchaseRateNB;
    }

    public void setPurchaseRateNB(double purchaseRateNB) {
        this.purchaseRateNB = purchaseRateNB;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    @Override
    public String toString() {
        return
                "ExchangeRateItem{" +
                        "saleRateNB = '" + saleRateNB + '\'' +
                        ",purchaseRateNB = '" + purchaseRateNB + '\'' +
                        ",currency = '" + currency + '\'' +
                        ",baseCurrency = '" + baseCurrency + '\'' +
                        "}";
    }
}