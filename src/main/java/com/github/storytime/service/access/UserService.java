package com.github.storytime.service.access;

import com.github.storytime.model.db.AppUser;
import com.github.storytime.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(final UserRepository repository) {
        this.repository = repository;
    }

    public List<AppUser> findAll() {
        return repository.findAll();
    }

    public Optional<AppUser> updateUserLastZenSyncTime(final AppUser u) {
        return of(repository.save(u));
    }

}
