package com.github.storytime.model.ynab.budget;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
@Deprecated
@Generated("com.robohorse.robopojogenerator")
public class YnabBudgets {

    @JsonProperty("last_modified_on")
    private String lastModifiedOn;

    @JsonProperty("first_month")
    private String firstMonth;

    @JsonProperty("last_month")
    private String lastMonth;

    @JsonProperty("name")
    private String name;

    @JsonProperty("date_format")
    private YnabBudgetDateFormat ynabBudgetDateFormat;

    @JsonProperty("id")
    private String id;

    @JsonProperty("currency_format")
    private YnabBudgetCurrencyFormat ynabBudgetCurrencyFormat;

    public String getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(String lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public String getFirstMonth() {
        return firstMonth;
    }

    public void setFirstMonth(String firstMonth) {
        this.firstMonth = firstMonth;
    }

    public String getLastMonth() {
        return lastMonth;
    }

    public void setLastMonth(String lastMonth) {
        this.lastMonth = lastMonth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public YnabBudgetDateFormat getYnabBudgetDateFormat() {
        return ynabBudgetDateFormat;
    }

    public void setYnabBudgetDateFormat(YnabBudgetDateFormat ynabBudgetDateFormat) {
        this.ynabBudgetDateFormat = ynabBudgetDateFormat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public YnabBudgetCurrencyFormat getYnabBudgetCurrencyFormat() {
        return ynabBudgetCurrencyFormat;
    }

    public void setYnabBudgetCurrencyFormat(YnabBudgetCurrencyFormat ynabBudgetCurrencyFormat) {
        this.ynabBudgetCurrencyFormat = ynabBudgetCurrencyFormat;
    }

    @Override
    public String toString() {
        return
                "YnabBudgets{" +
                        "last_modified_on = '" + lastModifiedOn + '\'' +
                        ",first_month = '" + firstMonth + '\'' +
                        ",last_month = '" + lastMonth + '\'' +
                        ",name = '" + name + '\'' +
                        ",date_format = '" + ynabBudgetDateFormat + '\'' +
                        ",id = '" + id + '\'' +
                        ",currency_format = '" + ynabBudgetCurrencyFormat + '\'' +
                        "}";
    }
}