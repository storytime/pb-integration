package com.github.storytime.model.ynab.common;

public class ZenYnabAccountReconcileProxyObject {

    private String account;
    private String zenAmount;
    private String ynabAmount;
    private String pbAmount;
    private String pbZenDiff;
    private String zenYnabDiff;
    private String status;

    public ZenYnabAccountReconcileProxyObject(String account,
                                              String zenAmount,
                                              String ynabAmount,
                                              String pbAmount,
                                              String pbZenDiff,
                                              String zenYnabDiff,
                                              String status) {
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
