package com.flowforge.backend.workflow.kafka;

import com.flowforge.backend.common.exception.ResourceNotFoundException;
import com.flowforge.backend.workflow.execution.DistributedLockService;
import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionRequest;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecution;
import com.flowforge.backend.workflow.execution.entity.WorkflowExecutionStatus;
import com.flowforge.backend.workflow.execution.repository.WorkflowExecutionRepository;
import com.flowforge.backend.workflow.outbox.WorkflowExecutionRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionConsumer {

    private final ObjectMapper objectMapper;
    private final WorkflowExecutionRepository executionRepository;
    private final WorkflowExecutionService executionService;
    private final DistributedLockService distributedLockService;

    @RetryableTopic(
        attempts = "4"
    )
    @KafkaListener(
        topics = "${flowforge.kafka.workflow-execution-topic}",
        groupId = "flowforge-workflow-executor"
    )
    public void consume(String payload) {

        try {

            WorkflowExecutionRequestedEvent event =
                objectMapper.readValue(
                    payload,
                    WorkflowExecutionRequestedEvent.class
                );

            log.info(
                "WORKFLOW_EXECUTION_MESSAGE_RECEIVED | executionId={}",
                event.executionId()
            );

            WorkflowExecution execution =
                executionRepository
                    .findById(event.executionId())
                    .orElseThrow(() ->
                        new ResourceNotFoundException(
                            "Workflow execution not found: "
                                + event.executionId()
                        )
                    );

            /*
             * Already completed.
             *
             * This protects against duplicate Kafka delivery.
             */
            if (
                execution.getStatus()
                    == WorkflowExecutionStatus.SUCCESS
                    ||
                    execution.getStatus()
                        == WorkflowExecutionStatus.FAILED
            ) {

                log.info(
                    "WORKFLOW_EXECUTION_ALREADY_COMPLETED | executionId={}",
                    event.executionId()
                );

                return;
            }

            /*
             * Atomically claim:
             *
             * QUEUED → RUNNING
             *
             * Only one consumer can successfully claim it.
             */
            int claimed =
                executionRepository.claimExecution(
                    event.executionId(),
                    Instant.now()
                );

            log.info(
                "WORKFLOW_EXECUTION_CLAIM_RESULT | executionId={} | claimed={}",
                event.executionId(),
                claimed
            );

            if (claimed == 0) {

                log.info(
                    "WORKFLOW_EXECUTION_ALREADY_CLAIMED | executionId={}",
                    event.executionId()
                );

                return;
            }

            String lockKey =
                "workflow:execution:lock:"
                    + event.executionId();

            String lockValue =
                distributedLockService.tryAcquire(
                    lockKey
                );

            if (lockValue == null) {

                log.warn(
                    "WORKFLOW_EXECUTION_LOCK_BUSY | executionId={}",
                    event.executionId()
                );

                throw new IllegalStateException(
                    "Workflow execution is currently locked"
                );
            }

            try {

                log.info(
                    "WORKFLOW_EXECUTION_LOCKED | executionId={}",
                    event.executionId()
                );

                executionService.execute(
                    event.executionId(),
                    event.workflowVersionId(),
                    new WorkflowExecutionRequest(
                        event.input()
                    )
                );

            } finally {

                distributedLockService.release(
                    lockKey,
                    lockValue
                );
            }

        } catch (Exception exception) {

            log.error(
                "WORKFLOW_EXECUTION_CONSUMER_FAILED",
                exception
            );

            /*
             * IMPORTANT:
             *
             * Do not swallow this exception.
             * Throwing it tells Spring Kafka that the
             * message failed and should be retried.
             */
            throw exception;
        }
    }

    @DltHandler
    public void handleDlt(String payload) {

        log.error(
            "WORKFLOW_EXECUTION_MOVED_TO_DLT | payload={}",
            payload
        );

        try {

            WorkflowExecutionRequestedEvent event =
                objectMapper.readValue(
                    payload,
                    WorkflowExecutionRequestedEvent.class
                );

            executionRepository.markFailed(
                event.executionId(),
                "Workflow execution moved to dead-letter topic"
            );

        } catch (Exception exception) {

            log.error(
                "WORKFLOW_EXECUTION_DLT_PROCESSING_FAILED",
                exception
            );
        }
    }
}
