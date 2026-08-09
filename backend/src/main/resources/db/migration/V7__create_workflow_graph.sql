CREATE TABLE workflow_versions
(
    id UUID PRIMARY KEY,

    workflow_id UUID NOT NULL,

    version_number INTEGER NOT NULL,

    published BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workflow_versions_workflow
        FOREIGN KEY (workflow_id)
            REFERENCES workflows(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_workflow_version_number
        UNIQUE (workflow_id, version_number)
);

CREATE INDEX idx_workflow_versions_workflow_id
    ON workflow_versions(workflow_id);


CREATE TABLE workflow_nodes
(
    id UUID PRIMARY KEY,

    workflow_version_id UUID NOT NULL,

    node_key VARCHAR(100) NOT NULL,

    name VARCHAR(150) NOT NULL,

    type VARCHAR(30) NOT NULL,

    connector_id UUID,

    configuration TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workflow_nodes_version
        FOREIGN KEY (workflow_version_id)
            REFERENCES workflow_versions(id)
            ON DELETE CASCADE,

    CONSTRAINT uk_workflow_node_key
        UNIQUE (workflow_version_id, node_key),

    CONSTRAINT chk_workflow_node_type
        CHECK (
            type IN (
                     'TRIGGER',
                     'HTTP_REQUEST',
                     'AI_PROMPT',
                     'CONDITION',
                     'TRANSFORM',
                     'DELAY'
                )
            )
);

CREATE INDEX idx_workflow_nodes_version_id
    ON workflow_nodes(workflow_version_id);


CREATE TABLE workflow_edges
(
    id UUID PRIMARY KEY,

    workflow_version_id UUID NOT NULL,

    source_node_id UUID NOT NULL,

    target_node_id UUID NOT NULL,

    condition VARCHAR(100),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_workflow_edges_version
        FOREIGN KEY (workflow_version_id)
            REFERENCES workflow_versions(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workflow_edges_source
        FOREIGN KEY (source_node_id)
            REFERENCES workflow_nodes(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workflow_edges_target
        FOREIGN KEY (target_node_id)
            REFERENCES workflow_nodes(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_workflow_edge_nodes
        CHECK (source_node_id <> target_node_id)
);

CREATE INDEX idx_workflow_edges_version_id
    ON workflow_edges(workflow_version_id);

CREATE INDEX idx_workflow_edges_source_node
    ON workflow_edges(source_node_id);

CREATE INDEX idx_workflow_edges_target_node
    ON workflow_edges(target_node_id);
