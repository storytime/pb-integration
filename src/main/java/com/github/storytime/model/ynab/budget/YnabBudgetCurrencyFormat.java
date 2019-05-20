package com.github.storytime.model.ynab.budget;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YnabBudgetCurrencyFormat {

    @JsonProperty("decimal_separator")
    private String decimalSeparator;

    @JsonProperty("group_separator")
    private String groupSeparator;

    @JsonProperty("symbol_first")
    private boolean symbolFirst;

    @JsonProperty("currency_symbol")
    private String currencySymbol;

    @JsonProperty("decimal_digits")
    private int decimalDigits;

    @JsonProperty("display_symbol")
    private boolean displaySymbol;

    @JsonProperty("iso_code")
    private String isoCode;

    @JsonProperty("example_format")
    private String exampleFormat;

    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public String getGroupSeparator() {
        return groupSeparator;
    }

    public void setGroupSeparator(String groupSeparator) {
        this.groupSeparator = groupSeparator;
    }

    public boolean isSymbolFirst() {
        return symbolFirst;
    }

    public void setSymbolFirst(boolean symbolFirst) {
        this.symbolFirst = symbolFirst;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public boolean isDisplaySymbol() {
        return displaySymbol;
    }

    public void setDisplaySymbol(boolean displaySymbol) {
        this.displaySymbol = displaySymbol;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getExampleFormat() {
        return exampleFormat;
    }

    public void setExampleFormat(String exampleFormat) {
        this.exampleFormat = exampleFormat;
    }

    @Override
    public String toString() {
        return
                "YnabBudgetCurrencyFormat{" +
                        "decimal_separator = '" + decimalSeparator + '\'' +
                        ",group_separator = '" + groupSeparator + '\'' +
                        ",symbol_first = '" + symbolFirst + '\'' +
                        ",currency_symbol = '" + currencySymbol + '\'' +
                        ",decimal_digits = '" + decimalDigits + '\'' +
                        ",display_symbol = '" + displaySymbol + '\'' +
                        ",iso_code = '" + isoCode + '\'' +
                        ",example_format = '" + exampleFormat + '\'' +
                        "}";
    }
}