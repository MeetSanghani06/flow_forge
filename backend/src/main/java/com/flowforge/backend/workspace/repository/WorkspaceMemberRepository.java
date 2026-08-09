package com.flowforge.backend.workspace.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workspace.entity.WorkspaceMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMemberRepository
    extends BaseRepository<WorkspaceMember> {

    List<WorkspaceMember> findAllByUserIdAndActiveTrue(UUID userId);

    boolean existsByWorkspaceIdAndUserId(
        UUID workspaceId,
        UUID userId
    );

    Optional<WorkspaceMember> findByWorkspaceIdAndUserIdAndActiveTrue(
        UUID workspaceId,
        UUID userId
    );
}
