package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowEdge;

import java.util.List;
import java.util.UUID;

public interface WorkflowEdgeRepository
    extends BaseRepository<WorkflowEdge> {

    List<WorkflowEdge> findAllByWorkflowVersionId(
        UUID workflowVersionId
    );
}
