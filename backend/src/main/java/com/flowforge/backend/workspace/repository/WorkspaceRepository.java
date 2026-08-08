package com.flowforge.backend.workspace.repository;

import com.flowforge.backend.common.persistence.BaseRepository;
import com.flowforge.backend.workspace.entity.Workspace;

import java.util.Optional;

public interface WorkspaceRepository
    extends BaseRepository<Workspace> {

    Optional<Workspace> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
