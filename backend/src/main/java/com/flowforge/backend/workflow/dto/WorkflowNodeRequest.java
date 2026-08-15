package com.flowforge.backend.workflow.dto;

import com.flowforge.backend.workflow.entity.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record WorkflowNodeRequest(

    @NotBlank
    @Size(max = 100)
    String nodeKey,

    @NotBlank
    @Size(max = 150)
    String name,

    @NotNull
    NodeType type,

    String connectorId,

    JsonNode configuration

) {
}
