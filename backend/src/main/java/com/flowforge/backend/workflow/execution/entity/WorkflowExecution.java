package com.flowforge.backend.workflow.execution.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import com.flowforge.backend.workflow.entity.WorkflowVersion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "workflow_executions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workflow_execution_idempotency",
            columnNames = {
                "workflow_version_id",
                "idempotency_key"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_workflow_executions_version_id",
            columnList = "workflow_version_id"
        ),
        @Index(
            name = "idx_workflow_executions_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_workflow_executions_started_at",
            columnList = "started_at"
        ),
        @Index(
            name = "idx_workflow_executions_idempotency",
            columnList = "workflow_version_id,idempotency_key"
        )
    }
)
@Getter
@Setter
public class WorkflowExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_version_id",
        nullable = false
    )
    private WorkflowVersion workflowVersion;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowExecutionStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
