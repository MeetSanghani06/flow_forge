package com.flowforge.backend.workspace.repository;

import com.flowforge.backend.common.persistence.BaseRepository;
import com.flowforge.backend.workspace.entity.WorkspaceMember;

import java.util.List;
import java.util.UUID;

public interface WorkspaceMemberRepository
    extends BaseRepository<WorkspaceMember> {

    List<WorkspaceMember> findAllByUserIdAndActiveTrue(UUID userId);

    boolean existsByWorkspaceIdAndUserId(
        UUID workspaceId,
        UUID userId
    );
}
