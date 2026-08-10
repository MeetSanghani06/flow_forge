package com.flowforge.backend.workflow.execution.dto;

import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowNodeExecutionResponse(
    UUID id,
    UUID workflowNodeId,
    String nodeKey,
    WorkflowNodeExecutionStatus status,
    Instant startedAt,
    Instant completedAt,
    String input,
    String output,
    String errorMessage
) {
}
