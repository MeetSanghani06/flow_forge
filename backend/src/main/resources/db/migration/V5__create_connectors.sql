CREATE TABLE connectors
(
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    type VARCHAR(30) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    configuration_ref VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_connectors_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_connector_workspace_name
        UNIQUE (workspace_id, name),

    CONSTRAINT chk_connector_type
        CHECK (
            type IN (
                     'HTTP',
                     'WEBHOOK',
                     'DATABASE',
                     'SLACK',
                     'GITHUB',
                     'GOOGLE'
                )
            )
);

CREATE INDEX idx_connectors_workspace_id
    ON connectors(workspace_id);
