ALTER TABLE workflows
DROP CONSTRAINT chk_workflow_status;

ALTER TABLE workflows
    ADD CONSTRAINT chk_workflow_status
        CHECK (
            status IN (
                       'DRAFT',
                       'ACTIVE',
                       'PUBLISHED',
                       'ARCHIVED'
                )
            );
