package com.github.storytime.model.api;

import java.util.List;

public class SavingsInfoResponse {

    private List<SavingsInfo> savings;
    private String total;

    public List<SavingsInfo> getSavings() {
        return savings;
    }

    public SavingsInfoResponse setSavings(final List<SavingsInfo> savings) {
        this.savings = savings;
        return this;
    }

    public String getTotal() {
        return total;
    }

    public SavingsInfoResponse setTotal(final String total) {
        this.total = total;
        return this;
    }
}
