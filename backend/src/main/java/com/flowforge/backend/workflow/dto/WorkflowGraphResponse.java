package com.flowforge.backend.workflow.dto;

import com.flowforge.backend.workflow.entity.NodeType;

import java.util.List;
import java.util.UUID;

public record WorkflowGraphResponse(

    UUID workflowVersionId,

    int versionNumber,

    List<NodeResponse> nodes,

    List<EdgeResponse> edges

) {

    public record NodeResponse(
        UUID id,
        String nodeKey,
        String name,
        NodeType type,
        UUID connectorId,
        String configuration
    ) {
    }

    public record EdgeResponse(
        UUID id,
        String source,
        String target,
        String condition
    ) {
    }
}
