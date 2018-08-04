package com.github.storytime.repository;

import com.github.storytime.model.db.MerchantInfo;
import com.github.storytime.model.db.inner.SyncPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface MerchantRepository extends JpaRepository<MerchantInfo, Long> {

    List<MerchantInfo> findAllByEnabledIsTrueAndSyncPriority(final SyncPriority syncPriority);

    List<MerchantInfo> findAllByEnabledIsTrueAndSyncPriorityIsNull();
}
