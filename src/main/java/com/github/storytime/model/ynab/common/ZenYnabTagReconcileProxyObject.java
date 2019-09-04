package com.github.storytime.model.ynab.common;

import java.math.BigDecimal;

public class ZenYnabTagReconcileProxyObject {

    private String category;
    private String zenAmount;
    private String ynabAmount;
    private String diff;

    public ZenYnabTagReconcileProxyObject(String category,
                                          Double zenAmount,
                                          Double ynabAmount) {
        this.category = category;
        this.zenAmount = String.valueOf(zenAmount);
        this.ynabAmount = String.valueOf(ynabAmount);

        final BigDecimal zenAmountBd = BigDecimal.valueOf(zenAmount);
        final BigDecimal ynabAmountBd = BigDecimal.valueOf(ynabAmount);
        this.diff = ynabAmountBd.subtract(zenAmountBd).toString();
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
