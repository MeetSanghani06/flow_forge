CREATE TABLE workflow_outbox_events (
                                        id UUID PRIMARY KEY,
                                        aggregate_type VARCHAR(100) NOT NULL,
                                        aggregate_id UUID NOT NULL,
                                        event_type VARCHAR(100) NOT NULL,
                                        payload TEXT NOT NULL,
                                        status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        published_at TIMESTAMP WITH TIME ZONE,
                                        retry_count INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_workflow_outbox_pending
    ON workflow_outbox_events(status, created_at);
