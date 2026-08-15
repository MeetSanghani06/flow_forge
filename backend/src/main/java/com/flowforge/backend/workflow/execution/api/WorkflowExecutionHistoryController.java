package com.flowforge.backend.workflow.execution.api;

import com.flowforge.backend.workflow.execution.WorkflowExecutionQueryService;
import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionResponse;
import com.flowforge.backend.workflow.execution.dto.WorkflowNodeExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow-executions")
@RequiredArgsConstructor
public class WorkflowExecutionHistoryController {

    private final WorkflowExecutionQueryService queryService;

    @GetMapping("/{executionId}")
    public WorkflowExecutionResponse getExecution(
        @PathVariable UUID executionId
    ) {

        return queryService.getExecution(
            executionId
        );
    }

    @GetMapping("/{executionId}/nodes")
    public List<WorkflowNodeExecutionResponse> getNodeExecutions(
        @PathVariable UUID executionId
    ) {

        return queryService.getNodeExecutions(
            executionId
        );
    }
}
