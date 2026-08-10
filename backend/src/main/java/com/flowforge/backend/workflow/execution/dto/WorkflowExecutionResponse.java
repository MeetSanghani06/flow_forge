package com.flowforge.backend.workflow.execution.dto;

import com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowExecutionResponse(
    UUID id,
    UUID workflowVersionId,
    WorkflowExecutionStatus status,
    Instant startedAt,
    Instant completedAt,
    String input,
    String output,
    String errorMessage
) {
}
