# FlowForge
# Master Architecture Handbook

**Version:** 1.0

**Status:** Final (Architecture Baseline)

**Audience:** Engineers, Reviewers, Hackathon Judges, Recruiters

---

# 1. Introduction

This document serves as the master architectural blueprint for FlowForge.

It describes the system from multiple perspectives, following the philosophy of the C4 Model and modern distributed systems design.

Every implementation decision in the codebase should trace back to this document.

---

# 2. Architectural Vision

FlowForge is an AI-powered business automation platform that allows users to build workflows using natural language or a visual builder.

The architecture is designed around four pillars:

- Build
- Execute
- Observe
- Improve

These pillars influence every module and every engineering decision.

---

# 3. High-Level Architecture

```
                        +-------------------------+
                        |        React UI         |
                        +-----------+-------------+
                                    |
                             REST / WebSocket
                                    |
                                    ▼
                    +-------------------------------+
                    |     Spring Boot Backend        |
                    | (Modular Monolith Architecture)|
                    +-------------------------------+
         Identity │ Workflow │ Execution │ AI │ Connector │ Monitoring
                    |
                    ▼
             Transactional Outbox
                    |
                    ▼
                Apache Kafka
      +-------------+-------------+--------------+
      |             |             |              |
 HTTP Worker   Email Worker   Delay Worker   AI Worker
      |             |             |              |
      +-------------+-------------+--------------+
                    |
                    ▼
             Execution Events
                    |
                    ▼
          Monitoring & Dashboard

Shared Infrastructure

- PostgreSQL
- Redis
- Spring AI Provider
- Docker Compose
- Prometheus
- Grafana
```

---

# 4. C4 Level 1 — System Context

## Actors

- Business User
- Developer
- Platform Engineer
- Administrator

## External Systems

- LLM Provider
- SMTP Server
- REST APIs
- Webhooks
- Slack / Discord
- Future Social Connectors

The FlowForge platform orchestrates all interactions while abstracting execution complexity from users.

---

# 5. C4 Level 2 — Containers

## React Application

Responsibilities:

- Authentication
- Workflow Builder
- AI Prompt Interface
- Dashboard
- Execution Timeline

---

## Spring Boot Backend

Responsibilities:

- Business Logic
- Workflow Validation
- API Layer
- Security
- AI Integration
- Event Publishing

---

## Kafka

Responsibilities:

- Event Transport
- Asynchronous Processing
- Replay Support
- Retry Coordination

---

## PostgreSQL

Stores:

- Users
- Workflows
- Workflow Versions
- Executions
- Outbox Events
- Connector Configurations

---

## Redis

Responsibilities:

- Workflow Cache
- Distributed Locks
- Rate Limiting
- Session Caching (optional)

---

## Prometheus

Collects runtime metrics.

---

## Grafana

Visualizes system health and execution metrics.

---

# 6. Backend Module Architecture

```
flowforge-backend
│
├── identity
├── workflow
├── execution
├── connector
├── ai
├── monitoring
├── infrastructure
└── shared
```

Each module owns:

- Controller
- Service
- Domain
- Repository
- Events
- DTOs
- Mapper
- Configuration

Modules communicate through domain services and events, avoiding tight coupling.

---

# 7. Request Lifecycle

### Workflow Creation

User submits a natural-language prompt.

↓

REST API

↓

Authentication

↓

Spring AI

↓

Structured Workflow JSON

↓

Validation

↓

Persist Workflow

↓

Return Visual Representation

---

### Workflow Execution

User clicks Execute.

↓

Execution Service

↓

Transactional Outbox

↓

Kafka

↓

Worker Selection

↓

Node Execution

↓

Execution Events

↓

Dashboard Update

---

# 8. Execution Engine

The execution engine is responsible for:

- DAG traversal
- Dependency resolution
- Parallel execution
- State transitions
- Retry scheduling
- Failure handling
- Replay

Execution states:

Pending → Running → Waiting → Retrying → Completed / Failed → Replay

---

# 9. Connector Framework

Each connector implements a common contract:

- validate()
- execute()
- supportsRetry()
- supportsCompensation() (future)

MVP Connectors:

- HTTP
- Email
- Delay
- AI Task
- Webhook
- Logger
- Slack/Discord

This design enables new connectors to be added without changing the workflow engine.

---

# 10. AI Architecture

Spring AI is used as an engineering assistant.

Capabilities:

- Workflow Generation
- Workflow Explanation
- Workflow Optimization
- Failure Analysis
- Documentation Generation
- Sample Payload Generation

AI output is always validated before persistence to ensure deterministic execution.

---

# 11. Messaging Architecture

Primary Topics

- workflow.triggered
- workflow.execution.started
- workflow.node.execute
- workflow.node.completed
- workflow.node.failed
- workflow.retry
- workflow.completed
- workflow.failed
- workflow.replay

Dead Letter Topics

- workflow.node.dlq
- connector.http.dlq
- connector.email.dlq

Each event includes:

- Event ID
- Correlation ID
- Workflow ID
- Execution ID
- Timestamp
- Version

---

# 12. Reliability Strategy

The platform demonstrates the following production patterns:

- Transactional Outbox
- Idempotency
- Exponential Backoff
- Dead Letter Queue
- Distributed Locking
- Circuit Breaker (connector level)
- Optimistic Locking
- Retry Policies

These patterns are first-class architectural concerns rather than afterthoughts.

---

# 13. Security Architecture

Authentication:

- JWT Access Token
- Refresh Token

Authorization:

- Role-Based Access Control

Additional Controls:

- Rate Limiting
- Input Validation
- Secrets via Environment Variables
- Audit Logging

---

# 14. Observability

Every execution is observable.

Captured telemetry includes:

- Workflow lifecycle
- Node execution timeline
- Retry attempts
- Failure reasons
- Worker status
- Kafka consumer lag (future)
- API latency
- Cache statistics

Dashboards provide both operational and business insights.

---

# 15. Deployment Architecture

Development:

Docker Compose launches:

- React
- Spring Boot
- PostgreSQL
- Redis
- Kafka
- Prometheus
- Grafana

Production (Future):

- Kubernetes
- Horizontal Worker Scaling
- Managed PostgreSQL
- Managed Kafka
- Centralized Logging

---

# 16. Scalability Strategy

Designed for horizontal growth:

- Stateless backend
- Independent workers
- Event-driven execution
- Cached workflow definitions
- Versioned APIs
- Connector abstraction

The modular monolith can evolve into microservices with minimal domain changes.

---

# 17. Failure Recovery

When a node fails:

1. Failure recorded.
2. Retry scheduled.
3. Exponential backoff applied.
4. Retry limit evaluated.
5. DLQ receives terminal failure.
6. AI analyzes root cause.
7. User corrects configuration.
8. Replay resumes execution safely.

This end-to-end recovery flow is a core demonstration scenario.

---

# 18. Engineering Standards

- Modular package structure
- API-first development
- Immutable DTOs where practical
- Constructor injection
- Structured logging
- Database migrations with Flyway
- Comprehensive API documentation
- Docker-first local development
- Clear separation of domain and infrastructure concerns

---

# 19. Architectural Roadmap

Phase 1 (Hackathon)

- Modular Monolith
- Event-Driven Execution
- AI Assistant
- Monitoring Dashboard
- Docker Deployment

Phase 2

- Plugin SDK
- Additional Connectors
- Workflow Marketplace
- Multi-Tenancy

Phase 3

- Microservice Extraction
- Kubernetes Deployment
- Event Sourcing
- AI Predictive Optimization

---

# 20. Conclusion

FlowForge is intentionally designed as a production-inspired platform rather than a feature demonstration.

The architecture balances rapid delivery with long-term maintainability, enabling a two-person team to complete a polished MVP within the hackathon while preserving a clear path toward future scalability.

Every component exists to support one of the platform's four guiding pillars:

**Build. Execute. Observe. Improve.**

This handbook serves as the architectural source of truth for implementation, code reviews, future enhancements, and technical discussions.