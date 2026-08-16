ALTER TABLE workflow_executions
    ADD COLUMN idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX uk_workflow_execution_idempotency
    ON workflow_executions (
                            workflow_version_id,
                            idempotency_key
        )
    WHERE idempotency_key IS NOT NULL;
