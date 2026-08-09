package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowVersionRepository
    extends BaseRepository<WorkflowVersion> {

    List<WorkflowVersion> findAllByWorkflowIdOrderByVersionNumberDesc(
        UUID workflowId
    );

    Optional<WorkflowVersion> findByWorkflowIdAndVersionNumber(
        UUID workflowId,
        int versionNumber
    );

    Optional<WorkflowVersion> findFirstByWorkflowIdOrderByVersionNumberDesc(
        UUID workflowId
    );
}
