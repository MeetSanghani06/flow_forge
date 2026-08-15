package com.flowforge.backend.auth.repository;

import com.flowforge.backend.auth.entity.User;
import com.flowforge.backend.common.entity.BaseRepository;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
