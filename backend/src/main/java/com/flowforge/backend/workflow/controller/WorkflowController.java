package com.flowforge.backend.workflow.controller;

import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.workflow.dto.CreateWorkflowRequest;
import com.flowforge.backend.workflow.dto.WorkflowResponse;
import com.flowforge.backend.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ApiResponse<WorkflowResponse> create(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @Valid @RequestBody CreateWorkflowRequest request
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            workflowService.create(
                workspaceId,
                userId,
                request
            )
        );
    }

    @GetMapping
    public ApiResponse<List<WorkflowResponse>> getWorkflows(
        Authentication authentication,
        @PathVariable UUID workspaceId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            workflowService.getWorkflows(
                workspaceId,
                userId
            )
        );
    }

    @GetMapping("/{workflowId}")
    public ApiResponse<WorkflowResponse> getWorkflow(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            workflowService.getWorkflow(
                workspaceId,
                workflowId,
                userId
            )
        );
    }
}
