package com.flowforge.backend.workflow.execution;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionPersistenceService {

    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowNodeExecutionRepository nodeExecutionRepository;

    @Transactional
    public WorkflowExecution startExecution(
        WorkflowVersion workflowVersion
    ) {

        WorkflowExecution execution =
            new WorkflowExecution();

        execution.setWorkflowVersion(workflowVersion);
        execution.setStatus(
            WorkflowExecutionStatus.RUNNING
        );
        execution.setStartedAt(Instant.now());

        return executionRepository.save(execution);
    }

    @Transactional
    public WorkflowNodeExecution startNodeExecution(
        WorkflowExecution execution,
        WorkflowNode node
    ) {

        WorkflowNodeExecution nodeExecution =
            new WorkflowNodeExecution();

        nodeExecution.setWorkflowExecution(execution);
        nodeExecution.setWorkflowNode(node);
        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.RUNNING
        );
        nodeExecution.setStartedAt(Instant.now());

        return nodeExecutionRepository.save(
            nodeExecution
        );
    }

    @Transactional
    public void completeNodeExecution(
        WorkflowNodeExecution nodeExecution,
        String output
    ) {

        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.SUCCESS
        );
        nodeExecution.setOutput(output);
        nodeExecution.setCompletedAt(Instant.now());

        nodeExecutionRepository.save(nodeExecution);
    }

    @Transactional
    public void failNodeExecution(
        WorkflowNodeExecution nodeExecution,
        String errorMessage
    ) {

        nodeExecution.setStatus(
            WorkflowNodeExecutionStatus.FAILED
        );
        nodeExecution.setErrorMessage(errorMessage);
        nodeExecution.setCompletedAt(Instant.now());

        nodeExecutionRepository.save(nodeExecution);
    }

    @Transactional
    public void completeExecution(
        WorkflowExecution execution,
        String output
    ) {

        execution.setStatus(
            WorkflowExecutionStatus.SUCCESS
        );
        execution.setOutput(output);
        execution.setCompletedAt(Instant.now());

        executionRepository.save(execution);
    }

    @Transactional
    public void failExecution(
        WorkflowExecution execution,
        String errorMessage
    ) {

        execution.setStatus(
            WorkflowExecutionStatus.FAILED
        );
        execution.setErrorMessage(errorMessage);
        execution.setCompletedAt(Instant.now());

        executionRepository.save(execution);
    }
}
