package com.flowforge.backend.workflow.execution.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import com.flowforge.backend.workflow.entity.WorkflowNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
    name = "workflow_node_executions",
    indexes = {
        @Index(
            name = "idx_node_executions_execution_id",
            columnList = "workflow_execution_id"
        ),
        @Index(
            name = "idx_node_executions_node_id",
            columnList = "workflow_node_id"
        ),
        @Index(
            name = "idx_node_executions_status",
            columnList = "status"
        )
    }
)
@Getter
@Setter
public class WorkflowNodeExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_execution_id",
        nullable = false
    )
    private WorkflowExecution workflowExecution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_node_id",
        nullable = false
    )
    private WorkflowNode workflowNode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowNodeExecutionStatus status;

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
