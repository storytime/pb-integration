package com.github.storytime.model.currency.minfin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinfinResponse {

    @JsonProperty("eur")
    private Eur eur;

    @JsonProperty("usd")
    private Usd usd;

    @JsonProperty("rub")
    private Rub rub;

    @Override
    public String toString() {
        return
                "MinfinResponse{" +
                        "eur = '" + eur + '\'' +
                        ",usd = '" + usd + '\'' +
                        ",rub = '" + rub + '\'' +
                        "}";
    }
}