package com.flowforge.backend.workflow.execution.dto;

public record NodeExecutionResult(
    String output
) {

    public static NodeExecutionResult empty() {
        return new NodeExecutionResult(null);
    }

    public static NodeExecutionResult of(
        String output
    ) {
        return new NodeExecutionResult(output);
    }
}
