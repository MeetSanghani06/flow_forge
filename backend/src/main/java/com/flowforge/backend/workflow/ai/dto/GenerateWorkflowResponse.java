package com.flowforge.backend.workflow.ai.dto;

import com.flowforge.backend.workflow.dto.WorkflowGraphResponse;

public record GenerateWorkflowResponse(

    String message,

    WorkflowGraphResponse graph

) {
}
