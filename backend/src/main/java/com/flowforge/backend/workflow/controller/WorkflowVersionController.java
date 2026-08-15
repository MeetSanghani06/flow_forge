package com.flowforge.backend.workflow.controller;

import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.workflow.dto.WorkflowVersionResponse;
import com.flowforge.backend.workflow.service.WorkflowVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    "/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/versions"
)
@RequiredArgsConstructor
public class WorkflowVersionController {

    private final WorkflowVersionService versionService;

    @PostMapping
    public ApiResponse<WorkflowVersionResponse> createVersion(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            versionService.createVersion(
                workspaceId,
                workflowId,
                userId
            )
        );
    }

    @GetMapping
    public ApiResponse<List<WorkflowVersionResponse>> getVersions(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            versionService.getVersions(
                workspaceId,
                workflowId,
                userId
            )
        );
    }

    @PostMapping("/{versionNumber}/clone")
    public ApiResponse<WorkflowVersionResponse> cloneVersion(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber,
        Authentication authentication
    ) {
        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            versionService.cloneVersion(
            workspaceId,
            workflowId,
            versionNumber,
            userId
            )
        );
    }

    @PostMapping(
        "/{versionNumber}/publish"
    )
    public WorkflowVersionResponse publishVersion(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber,
        Authentication authentication
    ) {
        UUID userId =
            UUID.fromString(authentication.getName());

        return versionService.publishVersion(
            workspaceId,
            workflowId,
            versionNumber,
            userId
        );
    }
}
