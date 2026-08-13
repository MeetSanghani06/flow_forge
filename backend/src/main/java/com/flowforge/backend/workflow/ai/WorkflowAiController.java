package com.flowforge.backend.workflow.ai;

import com.flowforge.backend.workflow.ai.dto.GenerateWorkflowRequest;
import com.flowforge.backend.workflow.ai.dto.GenerateWorkflowResponse;
import com.flowforge.backend.workflow.ai.dto.ModifyWorkflowRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WorkflowAiController {

    private final WorkflowAiService workflowAiService;

    @PostMapping(
        "/workspaces/{workspaceId}/workflows/{workflowId}" +
            "/versions/{versionNumber}/ai/generate"
    )
    public GenerateWorkflowResponse generateWorkflow(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber,
        @Valid @RequestBody GenerateWorkflowRequest request,
        Authentication authentication
    ) {

        UUID userId =
            UUID.fromString(
                authentication.getName()
            );

        return workflowAiService.generateWorkflow(
            workspaceId,
            workflowId,
            versionNumber,
            userId,
            request
        );
    }

    @PostMapping(
        "/workspaces/{workspaceId}/workflows/{workflowId}" +
            "/versions/{versionNumber}/ai/modify"
    )
    public GenerateWorkflowResponse modifyWorkflow(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @PathVariable int versionNumber,
        @Valid @RequestBody ModifyWorkflowRequest request,
        Authentication authentication
    ) {

        UUID userId =
            UUID.fromString(
                authentication.getName()
            );

        return workflowAiService.modifyWorkflow(
            workspaceId,
            workflowId,
            versionNumber,
            userId,
            request
        );
    }
}
