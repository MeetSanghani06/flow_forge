package com.flowforge.backend.workflow.dto;

import com.flowforge.backend.workflow.entity.WorkflowStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkflowResponse(

    UUID id,

    UUID workspaceId,

    String name,

    String description,

    WorkflowStatus status,

    Instant createdAt,

    Instant updatedAt

) {
}
