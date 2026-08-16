package com.flowforge.backend.workflow.outbox.entity;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
