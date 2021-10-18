package com.github.storytime.model.ynab.common;

import java.math.BigDecimal;

import static com.github.storytime.config.props.Constants.CURRENCY_SCALE;
import static java.math.RoundingMode.HALF_DOWN;
@Deprecated
public class ZenYnabTagReconcileProxyObject {

    private final String category;
    private final BigDecimal zenAmount;
    private final BigDecimal ynabAmount;
    private final String diff;

    public ZenYnabTagReconcileProxyObject(String category,
                                          BigDecimal zenAmount,
                                          BigDecimal ynabAmount) {
        this.category = category;
        this.zenAmount = zenAmount;
        this.ynabAmount = ynabAmount;
        this.diff = ynabAmount.subtract(zenAmount).setScale(CURRENCY_SCALE, HALF_DOWN).toString();
    }

    public String getCategory() {
        return category;
    }

    public String getZenAmountAsString() {
        return String.valueOf(zenAmount.setScale(CURRENCY_SCALE, HALF_DOWN));
    }

    public String getYnabAmountAsString() {
        return String.valueOf(ynabAmount.setScale(CURRENCY_SCALE, HALF_DOWN));
    }

    public BigDecimal getZenAmount() {
        return zenAmount;
    }

    public BigDecimal getYnabAmount() {
        return ynabAmount;
    }

    public String getDiff() {
        return diff;
    }

}
