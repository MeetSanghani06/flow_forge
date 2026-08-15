package com.flowforge.backend.workflow.execution.dto;

import java.util.Map;

public record WorkflowExecutionRequest(
    Map<String, Object> input
) {

    public static WorkflowExecutionRequest empty() {
        return new WorkflowExecutionRequest(
            Map.of()
        );
    }
}
