package com.flowforge.backend.workflow.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "workflow_nodes",
    indexes = {
        @Index(
            name = "idx_workflow_nodes_version_id",
            columnList = "workflow_version_id"
        )
    }
)
@Getter
@Setter
public class WorkflowNode extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_version_id",
        nullable = false
    )
    private WorkflowVersion workflowVersion;

    @Column(name = "node_key", nullable = false, length = 100)
    private String nodeKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NodeType type;

    @Column(name = "connector_id")
    private UUID connectorId;

    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;
}
