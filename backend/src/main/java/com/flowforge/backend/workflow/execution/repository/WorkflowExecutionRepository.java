package com.flowforge.backend.workflow.execution.repository;

import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowExecutionRepository
    extends JpaRepository<WorkflowExecution, UUID> {

    boolean existsByWorkflowVersionId(UUID workflowVersionId);
}
