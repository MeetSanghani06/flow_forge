package com.flowforge.backend.workflow.controller;

import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.workflow.dto.SaveWorkflowGraphRequest;
import com.flowforge.backend.workflow.dto.WorkflowGraphResponse;
import com.flowforge.backend.workflow.service.WorkflowGraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
    "/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/versions/{versionNumber}/graph"
)
@RequiredArgsConstructor
public class WorkflowGraphController {

    private final WorkflowGraphService graphService;

    @PutMapping
    public ApiResponse<WorkflowGraphResponse> saveGraph(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber,
        @Valid @RequestBody SaveWorkflowGraphRequest request
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            graphService.saveGraph(
                workspaceId,
                workflowId,
                versionNumber,
                userId,
                request
            )
        );
    }

    @GetMapping
    public ApiResponse<WorkflowGraphResponse> getGraph(
        Authentication authentication,
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber
    ) {

        UUID userId =
            UUID.fromString(authentication.getName());

        return ApiResponse.success(
            graphService.getGraph(
                workspaceId,
                workflowId,
                versionNumber,
                userId
            )
        );
    }
}
