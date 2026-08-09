package com.flowforge.backend.workflow.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkflowVersionResponse(

    UUID id,

    UUID workflowId,

    int versionNumber,

    boolean published,

    Instant createdAt,

    Instant updatedAt

) {
}
