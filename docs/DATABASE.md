# 🗄️ FlowForge Database Design

> **Database:** PostgreSQL 16+
>
> **Migration Tool:** Flyway
>
> **ORM:** Spring Data JPA + Hibernate

---

# 1. Database Philosophy

FlowForge uses a hybrid relational model.

Relational entities are normalized while dynamic workflow definitions and runtime contexts are stored as **JSONB**.

Benefits:

- Excellent querying
- Flexible workflow structure
- Faster development
- Easy versioning
- Reduced joins

---

# 2. Database Modules

Identity

↓

Workflow

↓

Execution

↓

Connector

↓

AI

↓

Infrastructure

---

# 3. Entity Relationship Diagram

```text
User
 │
 │ 1:N
 ▼
Workflow
 │
 │ 1:N
 ▼
WorkflowVersion
 │
 │ 1:N
 ▼
WorkflowExecution
 │
 │ 1:N
 ▼
NodeExecution

WorkflowVersion
 │
 │ 1:N
 ▼
WorkflowTemplate

WorkflowExecution
 │
 │ 1:N
 ▼
ExecutionEvent

WorkflowExecution
 │
 │ 1:N
 ▼
RetryRecord

WorkflowExecution
 │
 │ 1:N
 ▼
DLQRecord

Connector
 │
 │ 1:N
 ▼
ConnectorExecution

OutboxEvent
```

---

# 4. Tables

## users

Stores authenticated users.

| Column | Type |
|---------|------|
| id | UUID |
| name | VARCHAR |
| email | VARCHAR UNIQUE |
| password_hash | VARCHAR |
| role | ENUM |
| status | ENUM |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

---

## workflows

One logical workflow.

| Column | Type |
|---------|------|
| id | UUID |
| owner_id | UUID |
| name | VARCHAR |
| description | TEXT |
| latest_version | INT |
| status | ENUM |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

---

## workflow_versions

Immutable versions.

| Column | Type |
|---------|------|
| id | UUID |
| workflow_id | UUID |
| version | INT |
| definition | JSONB |
| checksum | VARCHAR |
| published_by | UUID |
| published_at | TIMESTAMP |

### JSON Example

```json
{
  "trigger": {},
  "nodes": [],
  "edges": []
}
```

---

## workflow_executions

Runtime instance.

| Column | Type |
|---------|------|
| id | UUID |
| workflow_version_id | UUID |
| correlation_id | UUID |
| state | ENUM |
| started_at | TIMESTAMP |
| completed_at | TIMESTAMP |
| execution_context | JSONB |
| retry_count | INT |
| current_node | VARCHAR |

---

## node_executions

Stores execution of every node.

| Column | Type |
|---------|------|
| id | UUID |
| execution_id | UUID |
| node_id | VARCHAR |
| connector_type | VARCHAR |
| state | ENUM |
| started_at | TIMESTAMP |
| finished_at | TIMESTAMP |
| input | JSONB |
| output | JSONB |
| error_message | TEXT |

---

## execution_events

Event timeline.

| Column | Type |
|---------|------|
| id | UUID |
| execution_id | UUID |
| event_type | VARCHAR |
| payload | JSONB |
| created_at | TIMESTAMP |

---

## retry_records

Retry history.

| Column | Type |
|---------|------|
| id | UUID |
| execution_id | UUID |
| node_id | VARCHAR |
| retry_number | INT |
| retry_time | TIMESTAMP |
| reason | TEXT |

---

## dlq_records

Dead Letter Queue persistence.

| Column | Type |
|---------|------|
| id | UUID |
| execution_id | UUID |
| node_id | VARCHAR |
| payload | JSONB |
| failure_reason | TEXT |
| replayed | BOOLEAN |
| created_at | TIMESTAMP |

---

## connectors

Connector metadata.

| Column | Type |
|---------|------|
| id | UUID |
| name | VARCHAR |
| type | VARCHAR |
| configuration | JSONB |
| enabled | BOOLEAN |

---

## connector_executions

Connector audit trail.

| Column | Type |
|---------|------|
| id | UUID |
| execution_id | UUID |
| connector_id | UUID |
| duration_ms | BIGINT |
| response | JSONB |
| status | ENUM |

---

## ai_requests

AI interaction history.

| Column | Type |
|---------|------|
| id | UUID |
| user_id | UUID |
| prompt | TEXT |
| response | JSONB |
| tokens_used | INT |
| latency_ms | BIGINT |
| created_at | TIMESTAMP |

---

## outbox_events

Transactional Outbox.

| Column | Type |
|---------|------|
| id | UUID |
| aggregate_type | VARCHAR |
| aggregate_id | UUID |
| event_type | VARCHAR |
| payload | JSONB |
| published | BOOLEAN |
| created_at | TIMESTAMP |

---

# 5. JSONB Strategy

Store dynamic structures in JSONB:

- Workflow Definition
- Execution Context
- Connector Configuration
- AI Responses
- Event Payloads
- Connector Outputs

Avoid storing relational data inside JSONB.

---

# 6. Versioning Strategy

Workflow versions are immutable.

Example:

Workflow

```
Order Automation
```

Versions

```
v1

v2

v3
```

Executing workflows always reference a specific version.

Older executions remain reproducible.

---

# 7. Indexing Strategy

## users

- email (UNIQUE)

## workflows

- owner_id
- status

## workflow_versions

- workflow_id
- (workflow_id, version) UNIQUE

## workflow_executions

- workflow_version_id
- correlation_id (UNIQUE)
- state
- started_at

## node_executions

- execution_id
- node_id
- state

## execution_events

- execution_id
- created_at

## retry_records

- execution_id

## dlq_records

- replayed
- created_at

## outbox_events

- published
- created_at

These indexes optimize the most common queries while keeping write overhead manageable.

---

# 8. Data Retention

| Table | Policy |
|--------|--------|
| workflow_executions | Retain indefinitely (MVP) |
| execution_events | Retain indefinitely (MVP) |
| retry_records | Retain indefinitely (MVP) |
| ai_requests | Retain indefinitely (MVP) |
| outbox_events | Archive after successful publication (future enhancement) |

---

# 9. Concurrency Strategy

- Optimistic locking on mutable aggregates.
- Immutable workflow versions.
- Correlation IDs to prevent duplicate execution.
- Redis-based distributed locks for execution coordination.

---

# 10. Audit Strategy

Every execution should be traceable.

Captured information includes:

- User
- Workflow Version
- Trigger Time
- Node Timeline
- Retry Attempts
- Failure Cause
- Replay History

---

# 11. Migration Strategy

Database schema is managed using Flyway.

Rules:

- One migration per logical change.
- Never edit an executed migration.
- Create new migrations for every modification.
- Seed reference data separately from schema changes.

---

# 12. Database Summary

The schema balances normalization with flexibility by using PostgreSQL JSONB where appropriate. Immutable workflow versioning, execution auditing, transactional outbox support, and optimized indexing provide a strong foundation for a production-inspired workflow automation platform while remaining practical for the hackathon MVP.