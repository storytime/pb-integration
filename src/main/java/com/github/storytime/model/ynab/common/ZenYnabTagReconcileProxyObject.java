package com.github.storytime.model.ynab.common;

import java.math.BigDecimal;

public class ZenYnabTagReconcileProxyObject {

    private String category;
    private String zenAmount;
    private String ynabAmount;
    private String diff;

    public ZenYnabTagReconcileProxyObject(String category,
                                          BigDecimal zenAmount,
                                          BigDecimal ynabAmount) {
        this.category = category;
        this.zenAmount = String.valueOf(zenAmount);
        this.ynabAmount = String.valueOf(ynabAmount);
        this.diff = ynabAmount.subtract(zenAmount).toString();
    }

    public String getCategory() {
        return category;
    }

    public String getZenAmount() {
        return zenAmount;
    }

    public String getYnabAmount() {
        return ynabAmount;
    }

    public String getDiff() {
        return diff;
    }

}
