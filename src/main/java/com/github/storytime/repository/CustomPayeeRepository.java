package com.github.storytime.repository;

import com.github.storytime.model.db.CustomPayee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CustomPayeeRepository extends JpaRepository<CustomPayee, Long> {

}
