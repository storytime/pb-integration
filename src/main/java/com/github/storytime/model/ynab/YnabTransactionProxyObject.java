package com.github.storytime.model.ynab;
@Deprecated
public class YnabTransactionProxyObject {

    private String categoryId;
    private Double amount;

    public YnabTransactionProxyObject(String categoryId, Double amount) {
        this.categoryId = categoryId;
        this.amount = amount;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public YnabTransactionProxyObject setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public YnabTransactionProxyObject setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YnabTransactionProxyObject)) return false;

        YnabTransactionProxyObject that = (YnabTransactionProxyObject) o;

        if (getCategoryId() != null ? !getCategoryId().equals(that.getCategoryId()) : that.getCategoryId() != null)
            return false;
        return getAmount() != null ? getAmount().equals(that.getAmount()) : that.getAmount() == null;

    }

    @Override
    public int hashCode() {
        int result = getCategoryId() != null ? getCategoryId().hashCode() : 0;
        result = 31 * result + (getAmount() != null ? getAmount().hashCode() : 0);
        return result;
    }
}
