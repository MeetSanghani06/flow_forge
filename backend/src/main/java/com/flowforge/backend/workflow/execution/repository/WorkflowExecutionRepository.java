package com.flowforge.backend.workflow.execution.repository;

import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowExecutionRepository
    extends JpaRepository<WorkflowExecution, UUID> {

    boolean existsByWorkflowVersionId(UUID workflowVersionId);

    Optional<WorkflowExecution> findById(UUID id);

    boolean existsByIdAndStatus(
        UUID id,
        WorkflowExecutionStatus status
    );

    @Modifying
    @Transactional
    @Query("""
    update WorkflowExecution e
       set e.status = com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus.RUNNING,
           e.startedAt = :startedAt
     where e.id = :executionId
       and e.status = com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus.QUEUED
""")
    int claimExecution(
        @Param("executionId") UUID executionId,
        @Param("startedAt") Instant startedAt
    );
}
