package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.Workflow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends BaseRepository<Workflow> {

    List<Workflow> findAllByWorkspaceId(UUID workspaceId);

    Optional<Workflow> findByIdAndWorkspaceId(
        UUID workflowId,
        UUID workspaceId
    );

    boolean existsByWorkspaceIdAndName(
        UUID workspaceId,
        String name
    );
}
