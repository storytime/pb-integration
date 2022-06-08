package com.github.storytime.model.currency.pb.cash;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashResponse {

    @JsonProperty("sale")
    private String sale;

    @JsonProperty("base_ccy")
    private String baseCcy;

    @JsonProperty("buy")
    private String buy;

    @JsonProperty("ccy")
    private String ccy;

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