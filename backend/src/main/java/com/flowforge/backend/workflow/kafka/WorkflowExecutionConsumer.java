package com.flowforge.backend.workflow.kafka;

import com.flowforge.backend.workflow.execution.WorkflowExecutionService;
import com.flowforge.backend.workflow.execution.dto.WorkflowExecutionRequest;
import com.flowforge.backend.workflow.execution.repository.WorkflowExecutionRepository;
import com.flowforge.backend.workflow.outbox.WorkflowExecutionRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionConsumer {

    private final WorkflowExecutionService executionService;
    private final WorkflowExecutionRepository executionRepository;
    private final ObjectMapper objectMapper;

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

            // 1. Atomically claim the execution
            int claimed =
                executionRepository.claimExecution(
                    event.executionId(),
                    Instant.now()
                );

            // 2. Another consumer already claimed it
            if (claimed == 0) {

                log.info(
                    "WORKFLOW_EXECUTION_ALREADY_CLAIMED | executionId={}",
                    event.executionId()
                );

                return;
            }

            // 3. Only the consumer that successfully
            //    claimed the execution gets here
            log.info(
                "WORKFLOW_EXECUTION_CLAIMED | executionId={}",
                event.executionId()
            );

            executionService.execute(
                event.executionId(),
                event.workflowVersionId(),
                new WorkflowExecutionRequest(
                    event.input()
                )
            );

        } catch (Exception exception) {

            log.error(
                "WORKFLOW_EXECUTION_CONSUMER_FAILED",
                exception
            );

            throw new IllegalStateException(
                "Workflow execution consumer failed",
                exception
            );
        }
    }
}
