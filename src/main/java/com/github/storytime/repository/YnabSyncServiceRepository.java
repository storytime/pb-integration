package com.github.storytime.repository;

import com.github.storytime.model.db.YnabSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface YnabSyncServiceRepository extends JpaRepository<YnabSyncConfig, Long> {

    Optional<List<YnabSyncConfig>> findAllByEnabledIsTrueAndUserId(Long id);

}
