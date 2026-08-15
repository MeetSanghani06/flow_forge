package com.flowforge.backend.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWorkflowRequest(

    @NotBlank
    @Size(min = 2, max = 150)
    String name,

    @Size(max = 500)
    String description

) {
}
