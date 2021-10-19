package com.github.storytime.service.access;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.inner.SyncPriority;
import com.github.storytime.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.storytime.config.props.CacheNames.MERCHANT_CACHE;
import static java.util.Collections.emptyList;

@Service
public class MerchantService {

    private final MerchantRepository repository;

    @Autowired
    public MerchantService(final MerchantRepository repository) {
        this.repository = repository;
    }

    @Cacheable(MERCHANT_CACHE)
    public List<MerchantInfo> getAllEnabledMerchantsBySyncPriority(final SyncPriority syncPriority) {
        final var maybeAllByPrio = repository.findAllByEnabledIsTrueAndSyncPriority(syncPriority);
        return maybeAllByPrio.isEmpty() ? emptyList() : maybeAllByPrio.get();
    }

    @Cacheable(MERCHANT_CACHE)
    public List<MerchantInfo> getAllEnabledMerchantsWithPriority() {
        final var maybeWithoutPro = repository.findAllByEnabledIsTrueAndSyncPriorityIsNull();
        return maybeWithoutPro.isEmpty() ? emptyList() : maybeWithoutPro.get();
    }

    @Cacheable(MERCHANT_CACHE)
    //TODO: Need to add user id
    public List<MerchantInfo> getAllEnabledMerchants() {
        return repository.findAll();
    }

    @CacheEvict(value = MERCHANT_CACHE, allEntries = true)
    public void saveAll(final List<MerchantInfo> all) {
        repository.saveAll(all);
    }

    @CacheEvict(value = MERCHANT_CACHE, allEntries = true)
    public void save(final MerchantInfo obj) {
        repository.save(obj);
    }

}
