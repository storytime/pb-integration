package com.github.storytime.model.currency.pb.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateItem {

    @JsonProperty("saleRateNB")
    private double saleRateNB;

    @JsonProperty("purchaseRateNB")
    private double purchaseRateNB;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("baseCurrency")
    private String baseCurrency;


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