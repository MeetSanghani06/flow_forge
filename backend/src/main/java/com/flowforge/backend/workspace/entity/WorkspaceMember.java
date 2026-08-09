package com.flowforge.backend.workspace.entity;

import com.flowforge.backend.auth.entity.User;
import com.flowforge.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "workspace_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workspace_member",
            columnNames = {"workspace_id", "user_id"}
        )
    }
)
@Getter
@Setter
public class WorkspaceMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkspaceRole role;

    @Column(nullable = false)
    private boolean active = true;
}
