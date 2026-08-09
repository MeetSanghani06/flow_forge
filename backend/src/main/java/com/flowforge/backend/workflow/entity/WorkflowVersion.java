package com.flowforge.backend.workflow.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "workflow_versions",
    indexes = {
        @Index(
            name = "idx_workflow_versions_workflow_id",
            columnList = "workflow_id"
        )
    }
)
@Getter
@Setter
public class WorkflowVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(nullable = false)
    private boolean published = false;
}
