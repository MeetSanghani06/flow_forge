package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus;
import tools.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WorkflowExecutionResult {

    private final UUID executionId;

    private final boolean success;

    private final String status;

    private final String failedNode;

    private final String errorMessage;

    private JsonNode output;

    public static WorkflowExecutionResult success(
        UUID executionId,
        JsonNode output
    ) {
        return WorkflowExecutionResult.builder()
            .executionId(executionId)
            .success(true)
            .status(WorkflowExecutionStatus.SUCCESS.name())
            .output(output)
            .build();
    }

    public static WorkflowExecutionResult failure(
        UUID executionId,
        String failedNode,
        String errorMessage
    ) {
        return WorkflowExecutionResult.builder()
            .executionId(executionId)
            .success(false)
            .status(WorkflowExecutionStatus.FAILED.name())
            .failedNode(failedNode)
            .errorMessage(errorMessage)
            .build();
    }

    public static WorkflowExecutionResult queued(UUID id) {
        return WorkflowExecutionResult.builder()
            .executionId(id)
            .status(WorkflowExecutionStatus.QUEUED.name())
            .success(true)
            .build();
    }
}
