package com.flowforge.backend.workflow.entity;

import com.flowforge.backend.common.entity.BaseEntity;
import com.flowforge.backend.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "workflows",
    indexes = {
        @Index(
            name = "idx_workflows_workspace_id",
            columnList = "workspace_id"
        )
    }
)
@Getter
@Setter
public class Workflow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowStatus status = WorkflowStatus.DRAFT;
}
