package com.flowforge.backend.connector.entity;

import com.flowforge.backend.common.persistence.BaseEntity;
import com.flowforge.backend.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "connectors",
    indexes = {
        @Index(
            name = "idx_connectors_workspace_id",
            columnList = "workspace_id"
        )
    }
)
@Getter
@Setter
public class Connector extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConnectorType type;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * Reference to encrypted credentials/configuration.
     *
     * For the MVP this can remain nullable.
     * We will introduce proper secret storage later.
     */
    @Column(name = "configuration_ref", length = 255)
    private String configurationRef;
}
