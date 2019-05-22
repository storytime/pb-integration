package com.github.storytime.model.db;


import com.github.storytime.model.db.inner.YnabTagsSyncProperties;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

@Entity
public class YnabSyncConfig extends BaseEntity {

    @NotNull
    private Long userId;

    @NotNull
    private Long lastSync;

    @NotNull
    @Column(length = 1024)
    private String budgetName;

    @NotNull
    @Enumerated(STRING)
    @ElementCollection(fetch = EAGER)
    private List<YnabTagsSyncProperties> tagsSyncProperties;

    public Long getUserId() {
        return userId;
    }

    public YnabSyncConfig setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getLastSync() {
        return lastSync;
    }

    public YnabSyncConfig setLastSync(Long lastSync) {
        this.lastSync = lastSync;
        return this;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public YnabSyncConfig setBudgetName(String budgetName) {
        this.budgetName = budgetName;
        return this;
    }

    public List<YnabTagsSyncProperties> getTagsSyncProperties() {
        return tagsSyncProperties;
    }

    public YnabSyncConfig setTagsSyncProperties(List<YnabTagsSyncProperties> tagsSyncProperties) {
        this.tagsSyncProperties = tagsSyncProperties;
        return this;
    }
}
