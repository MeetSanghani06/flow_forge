package com.flowforge.backend.workflow.api;

import com.flowforge.backend.workflow.execution.WorkflowExecutionResult;
import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import com.flowforge.backend.workspace.service.WorkspaceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    @PostMapping("/workflows/{workflowVersionId}/execute")
    public WorkflowExecutionResult execute(
        @PathVariable UUID workflowVersionId
    ) {
        return executionService.execute(workflowVersionId);
    }

    @PostMapping(
        "/workspaces/{workspaceId}/workflows/{workflowId}/execute"
    )
    public WorkflowExecutionResult executeWorkflow(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        Authentication authentication
    ) {
        UUID userId =
            UUID.fromString(authentication.getName());

        return executionService.executeWorkflow(
            workspaceId,
            workflowId,
            userId
        );
    }
}
