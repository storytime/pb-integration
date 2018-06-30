package com.github.storytime.model.currency.pb.cash;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashResponse {

    @JsonProperty("sale")
    private String sale;

    @JsonProperty("base_ccy")
    private String baseCcy;

    @JsonProperty("buy")
    private String buy;

    @JsonProperty("ccy")
    private String ccy;

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }

    public String getBaseCcy() {
        return baseCcy;
    }

    public void setBaseCcy(String baseCcy) {
        this.baseCcy = baseCcy;
    }

    public String getBuy() {
        return buy;
    }

    public void setBuy(String buy) {
        this.buy = buy;
    }

    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    @Override
    public String toString() {
        return
                "CashResponse{" +
                        "sale = '" + sale + '\'' +
                        ",base_ccy = '" + baseCcy + '\'' +
                        ",buy = '" + buy + '\'' +
                        ",ccy = '" + ccy + '\'' +
                        "}";
    }
}