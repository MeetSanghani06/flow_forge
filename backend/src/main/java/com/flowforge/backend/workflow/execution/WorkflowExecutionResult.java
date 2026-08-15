package com.flowforge.backend.workflow.execution;

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
            .status("SUCCESS")
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
            .status("FAILED")
            .failedNode(failedNode)
            .errorMessage(errorMessage)
            .build();
    }
}
