package com.flowforge.backend.workflow.api;

import com.flowforge.backend.workflow.execution.WorkflowExecutionResult;
import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import com.flowforge.backend.workflow.entity.WorkflowEdge;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.repository.WorkflowEdgeRepository;
import com.flowforge.backend.workflow.repository.WorkflowNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workflows")
public class WorkflowExecutionController {

    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final WorkflowExecutionService executionService;

    @PostMapping("/{workflowVersionId}/execute")
    public WorkflowExecutionResult execute(
        @PathVariable UUID workflowVersionId
    ) {

        List<WorkflowNode> nodes =
            nodeRepository
                .findAllByWorkflowVersionId(
                    workflowVersionId
                );

        List<WorkflowEdge> edges =
            edgeRepository
                .findAllByWorkflowVersionId(
                    workflowVersionId
                );

        return executionService.execute(
            nodes,
            edges
        );
    }
}
