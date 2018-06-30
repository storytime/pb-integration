package com.github.storytime.model.currency.minfin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MinfinResponse {

    @JsonProperty("eur")
    private Eur eur;

    @JsonProperty("usd")
    private Usd usd;

    @JsonProperty("rub")
    private Rub rub;

    public Eur getEur() {
        return eur;
    }

    public void setEur(Eur eur) {
        this.eur = eur;
    }

    public Usd getUsd() {
        return usd;
    }

    public void setUsd(Usd usd) {
        this.usd = usd;
    }

    public Rub getRub() {
        return rub;
    }

    public void setRub(Rub rub) {
        this.rub = rub;
    }

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