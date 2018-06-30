package com.github.storytime.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PbInternalTransferInfoRepository {

    private Set<String> pbTransferInfo;

    @Autowired
    PbInternalTransferInfoRepository(Set<String> pbTransferInfo) {
        this.pbTransferInfo = pbTransferInfo;
    }

    public void save(String obj) {
        pbTransferInfo.add(obj);
    }

    public void remove(String obj) {
        pbTransferInfo.remove(obj);
    }

    public boolean isExist(String obj) {
        return pbTransferInfo.contains(obj);
    }

}
