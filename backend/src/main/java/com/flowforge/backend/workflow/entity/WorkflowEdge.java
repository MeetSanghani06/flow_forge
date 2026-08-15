package com.flowforge.backend.workflow.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "workflow_edges",
    indexes = {
        @Index(
            name = "idx_workflow_edges_version_id",
            columnList = "workflow_version_id"
        )
    }
)
@Getter
@Setter
public class WorkflowEdge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_version_id",
        nullable = false
    )
    private WorkflowVersion workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "source_node_id",
        nullable = false
    )
    private WorkflowNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "target_node_id",
        nullable = false
    )
    private WorkflowNode targetNode;

    @Column(length = 100)
    private String condition;
}
