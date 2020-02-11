package com.github.storytime.repository;

import com.github.storytime.model.db.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public interface UserRepository extends JpaRepository<AppUser, Long> {

}
