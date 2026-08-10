package com.flowforge.backend.workflow.repository;

import com.flowforge.backend.common.entity.BaseRepository;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
    select max(v.versionNumber)
    from WorkflowVersion v
    where v.workflow.id = :workflowId
    """)
    Optional<Integer> findMaxVersionNumberByWorkflowId(
        @Param("workflowId") UUID workflowId
    );
}
