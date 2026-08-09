package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowNodeRepository
    extends BaseRepository<WorkflowNode> {

    List<WorkflowNode> findAllByWorkflowVersionId(
        UUID workflowVersionId
    );

    Optional<WorkflowNode> findByWorkflowVersionIdAndNodeKey(
        UUID workflowVersionId,
        String nodeKey
    );
}
