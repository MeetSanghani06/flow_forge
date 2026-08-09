package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("""
    DELETE FROM WorkflowNode n
    WHERE n.workflowVersion.id = :workflowVersionId
""")
    void deleteAllByWorkflowVersionId(
        @Param("workflowVersionId") UUID workflowVersionId
    );
}
