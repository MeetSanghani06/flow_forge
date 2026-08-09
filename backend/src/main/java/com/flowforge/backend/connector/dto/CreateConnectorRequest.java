package com.flowforge.backend.connector.dto;

import com.flowforge.backend.connector.entity.ConnectorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateConnectorRequest(

    @NotBlank
    @Size(min = 2, max = 100)
    String name,

    @NotNull
    ConnectorType type

) {
}
