package com.flowforge.backend.workflow.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ModifyWorkflowRequest(

    @NotBlank
    String instruction

) {
}
