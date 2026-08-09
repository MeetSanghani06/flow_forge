package com.flowforge.backend.workflow.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaveWorkflowGraphRequest(

    @NotEmpty
    @Valid
    List<WorkflowNodeRequest> nodes,

    @Valid
    List<WorkflowEdgeRequest> edges

) {
}
