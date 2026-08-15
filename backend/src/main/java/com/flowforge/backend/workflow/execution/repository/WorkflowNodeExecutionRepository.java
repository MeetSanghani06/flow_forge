package com.flowforge.backend.workflow.execution.repository;

import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowNodeExecutionRepository
    extends JpaRepository<WorkflowNodeExecution, UUID> {

    List<WorkflowNodeExecution> findAllByWorkflowExecutionIdOrderByStartedAtAsc(
        UUID workflowExecutionId
    );
}
