package com.flowforge.backend.workflow.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus;
import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowNodeExecutionStatus;
import com.flowforge.backend.workflow.execution.repository.WorkflowExecutionRepository;
import com.flowforge.backend.workflow.execution.repository.WorkflowNodeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionPersistenceService {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowNodeExecutionRepository nodeExecutionRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowExecution startExecution(
        WorkflowVersion workflowVersion,
        Map<String, Object> input
    ) {

        WorkflowExecution execution =
            new WorkflowExecution();

        execution.setWorkflowVersion(
            workflowVersion
        );

        execution.setStatus(
            WorkflowExecutionStatus.RUNNING
        );

        execution.setStartedAt(
            Instant.now()
        );

        try {

            execution.setInput(
                objectMapper.writeValueAsString(
                    input == null
                        ? Map.of()
                        : input
                )
            );

        } catch (Exception exception) {

            throw new IllegalArgumentException(
                "Unable to serialize workflow input",
                exception
            );
        }

        return executionRepository.save(
            execution
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WorkflowNodeExecution startNodeExecution(
        UUID executionId,
        WorkflowNode node,
        String input
    ) {

        WorkflowExecution execution =
            executionRepository.getReferenceById(
                executionId
            );

        WorkflowNodeExecution nodeExecution =
            new WorkflowNodeExecution();

        nodeExecution.setWorkflowExecution(execution);
        nodeExecution.setWorkflowNode(node);
        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.RUNNING
        );
        nodeExecution.setStartedAt(
            Instant.now()
        );
        nodeExecution.setInput(input);

        return nodeExecutionRepository.save(
            nodeExecution
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeNodeExecution(
        UUID nodeExecutionId,
        String output
    ) {

        WorkflowNodeExecution nodeExecution =
            nodeExecutionRepository
                .findById(nodeExecutionId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Node execution not found: "
                            + nodeExecutionId
                    )
                );

        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.SUCCESS
        );
        nodeExecution.setOutput(output);
        nodeExecution.setCompletedAt(
            Instant.now()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failNodeExecution(
        UUID nodeExecutionId,
        String errorMessage
    ) {

        WorkflowNodeExecution nodeExecution =
            nodeExecutionRepository
                .findById(nodeExecutionId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Node execution not found: "
                            + nodeExecutionId
                    )
                );

        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.FAILED
        );
        nodeExecution.setErrorMessage(
            errorMessage
        );
        nodeExecution.setCompletedAt(
            Instant.now()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeExecution(
        UUID executionId,
        String output
    ) {

        WorkflowExecution execution =
            executionRepository
                .findById(executionId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Execution not found: "
                            + executionId
                    )
                );

        execution.setStatus(
            WorkflowExecutionStatus.SUCCESS
        );
        execution.setOutput(output);
        execution.setCompletedAt(
            Instant.now()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failExecution(
        UUID executionId,
        String errorMessage
    ) {

        WorkflowExecution execution =
            executionRepository
                .findById(executionId)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Execution not found: "
                            + executionId
                    )
                );

        execution.setStatus(
            WorkflowExecutionStatus.FAILED
        );
        execution.setErrorMessage(
            errorMessage
        );
        execution.setCompletedAt(
            Instant.now()
        );
    }
}
