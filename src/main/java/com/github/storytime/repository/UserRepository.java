package com.github.storytime.repository;

import com.github.storytime.model.db.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserRepository extends JpaRepository<AppUser, Long> {

}
