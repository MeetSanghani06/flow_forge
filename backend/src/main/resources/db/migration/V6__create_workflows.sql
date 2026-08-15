CREATE TABLE workflows
(
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    name VARCHAR(150) NOT NULL,

    description VARCHAR(500),

    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workflows_workspace
        FOREIGN KEY (workspace_id)
            REFERENCES workspaces(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_workflow_workspace_name
        UNIQUE (workspace_id, name),

    CONSTRAINT chk_workflow_status
        CHECK (
            status IN (
                       'DRAFT',
                       'ACTIVE',
                       'ARCHIVED'
                )
            )
);

CREATE INDEX idx_workflows_workspace_id
    ON workflows(workspace_id);
