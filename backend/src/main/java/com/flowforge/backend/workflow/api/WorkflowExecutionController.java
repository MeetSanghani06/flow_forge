package com.flowforge.backend.workflow.api;

import com.flowforge.backend.workflow.execution.WorkflowExecutionResult;
import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workflows")
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;

    @PostMapping("/{workflowVersionId}/execute")
    public WorkflowExecutionResult execute(
        @PathVariable UUID workflowVersionId
    ) {
        return executionService.execute(workflowVersionId);
    }
}
