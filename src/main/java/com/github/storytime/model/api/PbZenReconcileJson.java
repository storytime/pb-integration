package com.github.storytime.model.api;

import java.util.List;

public class PbZenReconcileJson {

    private List<PbZenReconcile> dataList;

    public PbZenReconcileJson(List<PbZenReconcile> dataList) {
        this.dataList = dataList;
    }

    public List<PbZenReconcile> getDataList() {
        return dataList;
    }

    public void setDataList(List<PbZenReconcile> dataList) {
        this.dataList = dataList;
    }
}
