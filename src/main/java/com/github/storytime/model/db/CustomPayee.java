package com.github.storytime.model.db;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"containsValue"})
})
public class CustomPayee extends BaseEntity {

    @NotNull
    private String payee;

    @NotNull
    private String containsValue;

    public CustomPayee(@NotNull String payee, @NotNull String containsValue) {
        this.payee = payee;
        this.containsValue = containsValue;
    }

    public CustomPayee() {
    }

    public String getPayee() {
        return payee;
    }

    public CustomPayee setPayee(String payee) {
        this.payee = payee;
        return this;
    }

    public String getContainsValue() {
        return containsValue;
    }

    public CustomPayee setContainsValue(String containsValue) {
        this.containsValue = containsValue;
        return this;
    }

}
