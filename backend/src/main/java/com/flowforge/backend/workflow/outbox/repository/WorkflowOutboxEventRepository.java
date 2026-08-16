package com.flowforge.backend.workflow.outbox.repository;

import com.flowforge.backend.workflow.outbox.entity.OutboxEventStatus;
import com.flowforge.backend.workflow.outbox.entity.WorkflowOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowOutboxEventRepository
    extends JpaRepository<WorkflowOutboxEvent, UUID> {

    List<WorkflowOutboxEvent>
    findTop100ByStatusOrderByCreatedAtAsc(
        OutboxEventStatus status
    );
}
