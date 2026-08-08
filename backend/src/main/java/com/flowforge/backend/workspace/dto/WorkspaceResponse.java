package com.flowforge.backend.workspace.dto;

import com.flowforge.backend.workspace.entity.WorkspaceRole;

import java.util.UUID;

public record WorkspaceResponse(

    UUID id,

    String name,

    String slug,

    WorkspaceRole role

) {
}
