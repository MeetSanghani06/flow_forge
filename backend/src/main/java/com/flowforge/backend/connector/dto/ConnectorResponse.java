package com.flowforge.backend.connector.dto;

import com.flowforge.backend.connector.entity.ConnectorType;

import java.util.UUID;

public record ConnectorResponse(

    UUID id,

    UUID workspaceId,

    String name,

    ConnectorType type,

    boolean active

) {
}
