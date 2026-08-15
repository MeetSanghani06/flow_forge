package com.flowforge.backend.auth.repository;

import com.flowforge.backend.auth.entity.RefreshToken;
import com.flowforge.backend.common.entity.BaseRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
    extends BaseRepository<RefreshToken> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteAllByUserId(UUID userId);
}
