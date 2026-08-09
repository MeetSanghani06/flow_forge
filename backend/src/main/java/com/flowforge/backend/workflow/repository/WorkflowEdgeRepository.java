package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkflowEdgeRepository
    extends BaseRepository<WorkflowEdge> {

    List<WorkflowEdge> findAllByWorkflowVersionId(
        UUID workflowVersionId
    );

    @Modifying
    @Query("""
    DELETE FROM WorkflowEdge e
    WHERE e.workflowVersion.id = :workflowVersionId
""")
    void deleteAllByWorkflowVersionId(
        @Param("workflowVersionId") UUID workflowVersionId
    );
}
