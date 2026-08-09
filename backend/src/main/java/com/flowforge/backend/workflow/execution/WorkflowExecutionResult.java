package com.flowforge.backend.workflow.execution;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowExecutionResult {

    private final boolean success;

    private final String status;

    private final String failedNode;

    private final String errorMessage;

    public static WorkflowExecutionResult success() {
        return WorkflowExecutionResult.builder()
            .success(true)
            .status("COMPLETED")
            .build();
    }

    public static WorkflowExecutionResult failure(
        String nodeKey,
        String errorMessage
    ) {
        return WorkflowExecutionResult.builder()
            .success(false)
            .status("FAILED")
            .failedNode(nodeKey)
            .errorMessage(errorMessage)
            .build();
    }
}
