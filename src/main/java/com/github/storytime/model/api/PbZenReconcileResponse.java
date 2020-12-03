package com.github.storytime.model.api;

import java.util.List;

public class PbZenReconcileResponse {

    private List<PbZenReconcile> dataList;

    public PbZenReconcileResponse(final List<PbZenReconcile> dataList) {
        this.dataList = dataList;
    }

    public List<PbZenReconcile> getDataList() {
        return dataList;
    }

    public void setDataList(final List<PbZenReconcile> dataList) {
        this.dataList = dataList;
    }
}
