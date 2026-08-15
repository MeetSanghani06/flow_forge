ALTER TABLE workflows
    ADD COLUMN active_version_id UUID;

ALTER TABLE workflows
    ADD CONSTRAINT fk_workflows_active_version
        FOREIGN KEY (active_version_id)
            REFERENCES workflow_versions(id);

CREATE INDEX idx_workflows_active_version_id
    ON workflows(active_version_id);
