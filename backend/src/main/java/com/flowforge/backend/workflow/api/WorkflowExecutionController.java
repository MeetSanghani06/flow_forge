package com.flowforge.backend.workflow.api;

import com.flowforge.backend.workflow.execution.WorkflowExecutionResult;
import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    @PostMapping("/workflows/{workflowVersionId}/execute")
    public WorkflowExecutionResult execute(
        @PathVariable UUID workflowVersionId,
        @RequestHeader(
            value = "Idempotency-Key",
            required = false
        )
        String idempotencyKey,
        @RequestBody(
            required = false
        )
        WorkflowExecutionRequest request
    ) {

        if (request == null) {
            request =
                WorkflowExecutionRequest.empty();
        }

        return executionService.requestExecution(
            workflowVersionId,
            request,
            idempotencyKey
        );
    }

    @PostMapping(
        "/workspaces/{workspaceId}/workflows/{workflowId}/execute"
    )
    public WorkflowExecutionResult executeWorkflow(
        @PathVariable UUID workspaceId,
        @PathVariable UUID workflowId,
        @RequestBody(
            required = false
        )
        WorkflowExecutionRequest request,
        @RequestHeader(
            value = "Idempotency-Key",
            required = false
        )
        String idempotencyKey,
        Authentication authentication
    ) {

        UUID userId =
            UUID.fromString(
                authentication.getName()
            );

        if (request == null) {
            request =
                WorkflowExecutionRequest.empty();
        }

        return executionService.executeWorkflow(
            workspaceId,
            workflowId,
            userId,
            request,
            idempotencyKey
        );
    }
}
