package com.flowforge.backend.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowEdgeRequest(

    @NotBlank
    @Size(max = 100)
    String source,

    @NotBlank
    @Size(max = 100)
    String target,

    @Size(max = 100)
    String condition

) {
}
