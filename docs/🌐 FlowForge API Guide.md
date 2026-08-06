# 🌐 FlowForge API Guide

> **Version:** 1.0  
> **API Style:** REST  
> **Authentication:** JWT Bearer Token  
> **Documentation:** OpenAPI 3.1 / Swagger UI

---

# 1. Overview

FlowForge exposes a RESTful API for authentication, workflow management, AI-assisted workflow generation, execution, monitoring, and replay.

Base URL:

```text
http://localhost:8080/api/v1
```

Authentication:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

# 2. Authentication APIs

## Register

```http
POST /auth/register
```

Request

```json
{
  "name": "Meet Sanghani",
  "email": "meet@example.com",
  "password": "Password@123"
}
```

Response

```json
{
  "userId": "...",
  "accessToken": "...",
  "refreshToken": "..."
}
```

---

## Login

```http
POST /auth/login
```

---

## Refresh Token

```http
POST /auth/refresh
```

---

## Logout

```http
POST /auth/logout
```

---

# 3. Workflow APIs

## Create Workflow

```http
POST /workflows
```

Creates a workflow manually using the visual builder.

---

## Generate Workflow with AI

```http
POST /workflows/ai
```

Example Request

```json
{
  "prompt": "When a new GitHub issue is created, notify Slack and send an email to the engineering manager."
}
```

Example Response

```json
{
  "workflowId": "...",
  "version": 1,
  "definition": {
    "trigger": {},
    "nodes": [],
    "edges": []
  }
}
```

---

## Get Workflow

```http
GET /workflows/{workflowId}
```

---

## Update Draft Workflow

```http
PUT /workflows/{workflowId}
```

---

## Publish Workflow

```http
POST /workflows/{workflowId}/publish
```

Creates an immutable workflow version ready for execution.

---

## List Workflows

```http
GET /workflows
```

Supports:

- Pagination
- Search
- Status filter

---

## Delete Workflow

```http
DELETE /workflows/{workflowId}
```

Soft delete.

---

# 4. Execution APIs

## Execute Workflow

```http
POST /executions
```

Request

```json
{
  "workflowVersionId": "...",
  "input": {
    "customerId": 101,
    "orderId": "ORD-1001"
  }
}
```

Response

```json
{
  "executionId": "...",
  "status": "QUEUED"
}
```

---

## Execution Status

```http
GET /executions/{executionId}
```

Returns current execution state and summary.

---

## Execution Timeline

```http
GET /executions/{executionId}/timeline
```

Returns node-by-node execution history.

---

## Replay Execution

```http
POST /executions/{executionId}/replay
```

Replays a failed execution from the last failed node.

---

## Cancel Execution *(Optional MVP Stretch)*

```http
POST /executions/{executionId}/cancel
```

---

# 5. Monitoring APIs

## Dashboard Summary

```http
GET /monitoring/dashboard
```

Returns:

- Total Executions
- Success Rate
- Failure Rate
- Active Executions
- Retry Count

---

## Recent Executions

```http
GET /monitoring/executions
```

Supports:

- Pagination
- Date range
- Status filter

---

## Failed Executions

```http
GET /monitoring/failures
```

---

## Retry Statistics

```http
GET /monitoring/retries
```

---

# 6. AI APIs

## Explain Workflow

```http
POST /ai/explain
```

Returns a natural-language explanation of the workflow.

---

## Optimize Workflow

```http
POST /ai/optimize
```

Returns suggestions for improving performance and reliability.

---

## Analyze Failure

```http
POST /ai/analyze
```

Request

```json
{
  "executionId": "..."
}
```

Returns:

- Root cause
- Possible fixes
- Retry recommendation

---

## Generate Test Payload

```http
POST /ai/test-data
```

Generates sample input for workflow testing.

---

# 7. Health APIs

## Application Health

```http
GET /health
```

Checks application readiness.

---

## Readiness Probe

```http
GET /health/readiness
```

---

## Liveness Probe

```http
GET /health/liveness
```

---

# 8. Common Response Format

Successful responses

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-08-06T12:30:00Z"
}
```

Error responses

```json
{
  "success": false,
  "error": {
    "code": "WORKFLOW_NOT_FOUND",
    "message": "Workflow does not exist"
  },
  "timestamp": "2026-08-06T12:30:00Z"
}
```

---

# 9. Error Codes

| Code | Description |
|------|-------------|
| INVALID_REQUEST | Validation failed |
| UNAUTHORIZED | Authentication required |
| FORBIDDEN | Access denied |
| WORKFLOW_NOT_FOUND | Workflow not found |
| WORKFLOW_NOT_PUBLISHED | Workflow is not published |
| EXECUTION_NOT_FOUND | Execution not found |
| EXECUTION_ALREADY_RUNNING | Duplicate execution |
| CONNECTOR_FAILED | Connector execution failed |
| AI_GENERATION_FAILED | AI response invalid |
| RATE_LIMIT_EXCEEDED | Too many requests |
| INTERNAL_SERVER_ERROR | Unexpected server error |

---

# 10. API Design Principles

- RESTful resource naming
- Versioned endpoints (`/api/v1`)
- Consistent response envelope
- Immutable published workflows
- Idempotent execution APIs using correlation IDs
- OpenAPI-first documentation
- Bean Validation for request DTOs
- Global exception handling with standardized error codes

---

# 11. Security

- JWT Bearer authentication
- BCrypt password hashing
- Role-based authorization
- Request validation
- Rate limiting
- Secure HTTP headers
- Environment-based secrets

---

# 12. API Roadmap

Future API enhancements:

- WebSocket execution events
- Server-Sent Events (SSE) for live updates
- Bulk workflow operations
- Connector Marketplace APIs
- Organization & Multi-tenancy APIs
- Public SDKs (Java, TypeScript)

---

# 13. Conclusion

The FlowForge API is designed to be predictable, versioned, and developer-friendly. While this guide documents the core contract, the Swagger UI generated from the Spring Boot application remains the authoritative source for endpoint definitions, request/response schemas, and interactive testing.