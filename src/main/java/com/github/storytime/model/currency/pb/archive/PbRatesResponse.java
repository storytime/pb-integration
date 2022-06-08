package com.github.storytime.model.currency.pb.archive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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