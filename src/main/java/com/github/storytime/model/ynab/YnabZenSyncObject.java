package com.github.storytime.model.ynab;

public class YnabZenSyncObject {

    private final String zenId;
    private final String ynabId;
    private String name;

    public YnabZenSyncObject(String zenId, String ynabId, String name) {
        this.zenId = zenId;
        this.ynabId = ynabId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public YnabZenSyncObject setName(String name) {
        this.name = name;
        return this;
    }

    public String getZenId() {
        return zenId;
    }

    public String getYnabId() {
        return ynabId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YnabZenSyncObject)) return false;

        YnabZenSyncObject that = (YnabZenSyncObject) o;

        if (!getZenId().equals(that.getZenId())) return false;
        return getYnabId().equals(that.getYnabId());

    }

    @Override
    public int hashCode() {
        int result = getZenId().hashCode();
        result = 31 * result + getYnabId().hashCode();
        return result;
    }
}
