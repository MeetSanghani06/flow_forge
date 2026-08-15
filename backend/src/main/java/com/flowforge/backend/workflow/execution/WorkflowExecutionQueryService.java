package com.flowforge.backend.workflow.execution;

import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionResponse;
import com.flowforge.backend.workflow.execution.dto.WorkflowNodeExecutionResponse;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.mapper.WorkflowExecutionMapper;
import com.flowforge.backend.workflow.execution.repository.WorkflowExecutionRepository;
import com.flowforge.backend.workflow.execution.repository.WorkflowNodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionQueryService {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowExecutionMapper executionMapper;
    private final WorkflowNodeExecutionRepository nodeExecutionRepository;

    @Transactional(readOnly = true)
    public WorkflowExecutionResponse getExecution(
        UUID executionId
    ) {

        WorkflowExecution execution =
            executionRepository
                .findById(executionId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Workflow execution not found: "
                            + executionId
                    )
                );

        return executionMapper.toResponse(execution);
    }

    @Transactional(readOnly = true)
    public List<WorkflowNodeExecutionResponse> getNodeExecutions(
        UUID executionId
    ) {

        return nodeExecutionRepository
            .findAllByWorkflowExecutionIdOrderByStartedAtAsc(
                executionId
            )
            .stream()
            .map(nodeExecution ->
                new WorkflowNodeExecutionResponse(
                    nodeExecution.getId(),
                    nodeExecution.getWorkflowNode().getId(),
                    nodeExecution.getWorkflowNode().getNodeKey(),
                    nodeExecution.getStatus(),
                    nodeExecution.getStartedAt(),
                    nodeExecution.getCompletedAt(),
                    nodeExecution.getInput(),
                    nodeExecution.getOutput(),
                    nodeExecution.getErrorMessage()
                )
            )
            .toList();
    }
}
