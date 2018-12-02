package com.github.storytime.service.access;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.inner.SyncPriority;
import com.github.storytime.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MerchantService {

    private final MerchantRepository repository;

    @Autowired
    public MerchantService(final MerchantRepository repository) {
        this.repository = repository;
    }

    public Optional<List<MerchantInfo>> getAllEnabledMerchantsBySyncPriority(final SyncPriority syncPriority) {
        return repository.findAllByEnabledIsTrueAndSyncPriority(syncPriority);
    }

    public Optional<List<MerchantInfo>> getAllEnabledMerchantsWithPriority() {
        return repository.findAllByEnabledIsTrueAndSyncPriorityIsNull();
    }

    public void saveAll(List<MerchantInfo> all) {
        repository.saveAll(all);
    }

    public void save(MerchantInfo obj) {
        repository.save(obj);
    }

}
