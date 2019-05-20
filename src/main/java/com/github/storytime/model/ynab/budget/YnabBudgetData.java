package com.github.storytime.model.ynab.budget;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class YnabBudgetData {

    @JsonProperty("budgets")
    private List<YnabBudgets> budgets;

    public List<YnabBudgets> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<YnabBudgets> budgets) {
        this.budgets = budgets;
    }

    @Override
    public String toString() {
        return
                "YnabAccountData{" +
                        "budgets = '" + budgets + '\'' +
                        "}";
    }
}