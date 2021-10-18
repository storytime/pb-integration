package com.github.storytime.model.ynab.common;
@Deprecated
public class ZenYnabAccountReconcileProxyObject {

    private final String account;
    private final String zenAmount;
    private final String ynabAmount;
    private final String pbAmount;
    private final String pbZenDiff;
    private final String zenYnabDiff;
    private final String status;

    public ZenYnabAccountReconcileProxyObject(final String account,
                                              final String zenAmount,
                                              final String ynabAmount,
                                              final String pbAmount,
                                              final String pbZenDiff,
                                              final String zenYnabDiff,
                                              final String status) {
        this.account = account;
        this.zenAmount = zenAmount;
        this.ynabAmount = ynabAmount;
        this.pbAmount = pbAmount;
        this.pbZenDiff = pbZenDiff;
        this.zenYnabDiff = zenYnabDiff;
        this.status = status;
    }


    public String getAccount() {
        return account;
    }

    public String getZenAmount() {
        return zenAmount;
    }

    public String getYnabAmount() {
        return ynabAmount;
    }

    public String getPbAmount() {
        return pbAmount;
    }

    public String getPbZenDiff() {
        return pbZenDiff;
    }

    public String getZenYnabDiff() {
        return zenYnabDiff;
    }

    public String getStatus() {
        return status;
    }
}
