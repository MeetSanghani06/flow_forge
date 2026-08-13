package com.flowforge.backend.workflow.outbox;

import java.util.Map;
import java.util.UUID;

public record WorkflowExecutionRequestedEvent(
    UUID executionId,
    UUID workflowVersionId,
    Map<String, Object> input
) {
}
