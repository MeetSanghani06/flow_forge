package com.flowforge.backend.workspace.controller;

import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.workspace.dto.CreateWorkspaceRequest;
import com.flowforge.backend.workspace.dto.WorkspaceResponse;
import com.flowforge.backend.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ApiResponse<WorkspaceResponse> create(
        Authentication authentication,
        @Valid @RequestBody CreateWorkspaceRequest request
    ) {

        UUID userId =
            UUID.fromString(
                authentication.getName()
            );

        return ApiResponse.success(
            workspaceService.create(
                userId,
                request
            )
        );
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> getMine(
        Authentication authentication
    ) {

        UUID userId =
            UUID.fromString(
                authentication.getName()
            );

        return ApiResponse.success(
            workspaceService.getUserWorkspaces(userId)
        );
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceResponse> getWorkspace(
        Authentication authentication,
        @PathVariable UUID workspaceId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            workspaceService.getWorkspace(
                workspaceId,
                userId
            )
        );
    }
}
