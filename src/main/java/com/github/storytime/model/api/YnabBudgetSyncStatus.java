package com.github.storytime.model.api;

public class YnabBudgetSyncStatus {

    private String name;

    private String status;

    public YnabBudgetSyncStatus(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public YnabBudgetSyncStatus setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public YnabBudgetSyncStatus setStatus(String status) {
        this.status = status;
        return this;
    }
}
