package com.flowforge.backend.workflow.outbox;

import com.flowforge.backend.workflow.outbox.entity.OutboxEventStatus;
import com.flowforge.backend.workflow.outbox.entity.WorkflowOutboxEvent;
import com.flowforge.backend.workflow.outbox.repository.WorkflowOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowOutboxService {

    private final WorkflowOutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createExecutionRequestedEvent(
        UUID executionId,
        UUID workflowVersionId
    ) {

        try {

            WorkflowExecutionRequestedEvent event =
                new WorkflowExecutionRequestedEvent(
                    executionId,
                    workflowVersionId
                );

            WorkflowOutboxEvent outbox =
                new WorkflowOutboxEvent();

            outbox.setAggregateType(
                "WORKFLOW_EXECUTION"
            );

            outbox.setAggregateId(
                executionId
            );

            outbox.setEventType(
                "WORKFLOW_EXECUTION_REQUESTED"
            );

            outbox.setPayload(
                objectMapper.writeValueAsString(event)
            );

            outbox.setStatus(
                OutboxEventStatus.PENDING
            );

            repository.save(outbox);

        } catch (Exception exception) {

            throw new IllegalStateException(
                "Failed to create workflow execution outbox event",
                exception
            );
        }
    }
}
