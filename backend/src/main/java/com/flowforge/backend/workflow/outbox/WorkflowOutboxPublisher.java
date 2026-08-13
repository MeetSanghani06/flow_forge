package com.flowforge.backend.workflow.outbox;

import com.flowforge.backend.workflow.outbox.entity.OutboxEventStatus;
import com.flowforge.backend.workflow.outbox.entity.WorkflowOutboxEvent;
import com.flowforge.backend.workflow.outbox.repository.WorkflowOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowOutboxPublisher {

    private final WorkflowOutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${flowforge.kafka.workflow-execution-topic}")
    private String topic;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {

        var events =
            repository
                .findTop100ByStatusOrderByCreatedAtAsc(
                    OutboxEventStatus.PENDING
                );

        for (WorkflowOutboxEvent event : events) {

            try {

                kafkaTemplate
                    .send(
                        topic,
                        event.getAggregateId().toString(),
                        event.getPayload()
                    )
                    .get();

                event.setStatus(
                    OutboxEventStatus.PUBLISHED
                );

                event.setPublishedAt(
                    java.time.Instant.now()
                );

                repository.save(event);

                log.info(
                    "OUTBOX_PUBLISHED | eventId={} | aggregateId={}",
                    event.getId(),
                    event.getAggregateId()
                );

            } catch (Exception exception) {

                event.setRetryCount(
                    event.getRetryCount() + 1
                );

                repository.save(event);

                log.error(
                    "OUTBOX_PUBLISH_FAILED | eventId={}",
                    event.getId(),
                    exception
                );
            }
        }
    }
}
