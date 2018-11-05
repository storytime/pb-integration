package com.github.storytime.model.db;


import com.github.storytime.model.db.inner.AdditionalComment;
import com.github.storytime.model.db.inner.SyncPriority;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

@Entity
public class MerchantInfo extends BaseEntity {

    @NotNull
    private String cardNumber;

    @NotNull
    private Integer merchantId;

    @NotNull
    private String password;

    @NotNull
    private Boolean enabled;

    @NotNull
    private Long syncStartDate;

    @NotNull
    private Long syncPeriod;

    @Enumerated(STRING)
    @ElementCollection(fetch = EAGER)
    private List<AdditionalComment> additionalComment;

    @Enumerated(STRING)
    private SyncPriority syncPriority;

    @Column(length = 128)
    private String shortDesc;

    public List<AdditionalComment> getAdditionalComment() {
        return additionalComment;
    }

    public MerchantInfo setAdditionalComment(List<AdditionalComment> additionalComment) {
        this.additionalComment = additionalComment;
        return this;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public MerchantInfo setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public MerchantInfo setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public MerchantInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public MerchantInfo setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Long getSyncStartDate() {
        return syncStartDate;
    }

    public MerchantInfo setSyncStartDate(Long syncStartDate) {
        this.syncStartDate = syncStartDate;
        return this;
    }

    public Long getSyncPeriod() {
        return syncPeriod;
    }

    public MerchantInfo setSyncPeriod(Long syncPeriod) {
        this.syncPeriod = syncPeriod;
        return this;
    }

    public SyncPriority getSyncPriority() {
        return syncPriority;
    }

    public MerchantInfo setSyncPriority(SyncPriority syncPriority) {
        this.syncPriority = syncPriority;
        return this;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public MerchantInfo setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
        return this;
    }
}
