package com.flowforge.backend.workspace.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workspace.entity.Workspace;

import java.util.Optional;

public interface WorkspaceRepository
    extends BaseRepository<Workspace> {

    Optional<Workspace> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
