package com.github.storytime.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PbInternalTransferInfoRepository {

    private Set<String> pbTransferInfoStorage;

    @Autowired
    PbInternalTransferInfoRepository(final Set<String> pbTransferInfoStorage) {
        this.pbTransferInfoStorage = pbTransferInfoStorage;
    }

    public void save(String obj) {
        pbTransferInfoStorage.add(obj);
    }

    public boolean isExist(String obj) {
        return pbTransferInfoStorage.contains(obj);
    }

}
