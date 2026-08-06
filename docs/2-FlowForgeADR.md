# FlowForge
# Architecture Decision Records (ADR)

**Version:** 1.0

**Status:** Final

**Authors:** Meet Sanghani & Team

---

# Purpose

This document captures all significant architectural decisions made while designing FlowForge.

Each decision follows the same format:

- Problem
- Decision
- Alternatives Considered
- Why We Chose It
- Trade-offs

These records ensure architectural consistency while serving as engineering documentation and interview reference material.

---

# ADR-001
## Modular Monolith for MVP

### Problem

Should FlowForge be implemented as multiple independently deployable microservices or as a modular monolith?

### Decision

Build the MVP as a **Modular Monolith** with clear module boundaries.

### Alternatives

- Full Microservices
- Modular Monolith
- Traditional Layered Monolith

### Why

The hackathon lasts only ten days.

Building true microservices would introduce unnecessary complexity:

- Service discovery
- Inter-service communication
- Independent deployment
- Network failures
- Distributed debugging

Instead, we isolate modules internally while preserving the ability to extract services later.

### Benefits

- Faster development
- Easier debugging
- Simpler deployment
- Lower operational overhead
- Cleaner local development

### Trade-offs

- Modules share one deployment.
- Independent scaling is deferred.
- Future extraction requires planning.

---

# ADR-002
## Event-Driven Architecture

### Problem

How should workflow execution be coordinated?

### Decision

Use an Event-Driven Architecture powered by Kafka.

### Alternatives

- REST-only communication
- RabbitMQ
- Kafka
- Database polling

### Why

Workflow execution is asynchronous by nature.

Kafka provides:

- Durable messaging
- High throughput
- Ordering guarantees
- Replay capability
- Consumer groups

### Trade-offs

- Higher operational complexity
- More infrastructure
- Learning curve

---

# ADR-003
## PostgreSQL as Primary Database

### Decision

Use PostgreSQL as the primary persistence layer.

### Alternatives

- MongoDB
- MySQL
- PostgreSQL

### Why

The domain is highly relational.

Examples

- Workflows
- Versions
- Executions
- Users
- Connectors

Strong ACID guarantees are more valuable than schema flexibility.

### Trade-offs

Complex JSON structures require additional modeling.

---

# ADR-004
## Redis for Caching and Distributed Locking

### Decision

Redis will serve two responsibilities.

- Caching
- Distributed Locks

### Why

Workflow definitions are frequently read.

Execution coordination requires distributed locking.

Redis solves both efficiently.

### Trade-offs

Additional infrastructure.

---

# ADR-005
## Spring AI for Intelligent Assistance

### Decision

Use Spring AI as the AI abstraction layer.

### Alternatives

- Direct OpenAI SDK
- LangChain
- Spring AI

### Why

FlowForge is a Spring Boot project.

Spring AI integrates naturally with

- Dependency Injection
- Configuration
- Structured Outputs
- Prompt Templates

This keeps the architecture consistent.

### Trade-offs

Spring AI evolves rapidly, so APIs may change over time.

---

# ADR-006
## Connector-Based Architecture

### Problem

How should external integrations be handled?

### Decision

Implement connectors using a common interface.

Every connector implements:

- validate()
- execute()
- retry()
- compensate() (future)

### Benefits

- Extensible
- Testable
- Easy to add new integrations

### Examples

- HTTP
- Email
- Slack
- AI
- Delay
- Webhook

Future connectors require minimal framework changes.

---

# ADR-007
## DAG-Based Workflow Execution

### Decision

Represent workflows as Directed Acyclic Graphs (DAGs).

### Why

DAGs naturally model:

- Dependencies
- Parallel execution
- Validation
- Scheduling

### Alternatives

- Linked list
- State machine
- Tree

DAGs provide the greatest flexibility.

---

# ADR-008
## Outbox Pattern

### Problem

Database updates and Kafka publishing must remain consistent.

### Decision

Implement the Transactional Outbox Pattern.

### Why

Avoids dual-write inconsistencies.

Ensures reliable event publishing.

### Trade-offs

Extra table.

Additional publisher process.

---

# ADR-009
## Idempotent Workflow Execution

### Decision

Every workflow execution receives a unique execution identifier.

Workers reject duplicate processing.

### Why

Kafka guarantees at-least-once delivery.

Idempotency guarantees exactly-once business behavior.

---

# ADR-010
## Retry with Exponential Backoff

### Decision

Transient failures automatically retry.

Delay increases exponentially.

### Benefits

- Prevents cascading failures.
- Protects downstream services.

---

# ADR-011
## Dead Letter Queue

### Decision

Failed messages exceeding retry limits move to a DLQ.

### Why

Operations teams should inspect failures rather than lose them.

Replay becomes possible.

---

# ADR-012
## JWT Authentication

### Decision

Use JWT Access Tokens with Refresh Tokens.

### Why

Stateless authentication.

Simple deployment.

Scalable architecture.

---

# ADR-013
## API-First Development

### Decision

Design OpenAPI specifications before implementing controllers.

### Benefits

- Better frontend/backend collaboration
- Improved documentation
- Easier testing

---

# ADR-014
## React Flow for Workflow Builder

### Decision

Use React Flow to build the visual workflow editor.

### Why

Provides a mature node-based UI.

Supports drag-and-drop, zooming, and custom nodes.

### Trade-offs

Adds frontend dependency.

---

# ADR-015
## Docker-First Development

### Decision

Every service required by FlowForge runs through Docker Compose.

### Benefits

- One-command setup
- Consistent environments
- Easier judging
- Easier onboarding

---

# ADR-016
## Observability by Default

### Decision

Every workflow execution must be observable.

Metrics

Logs

Execution Timeline

Worker Status

Retry Count

Failure Reasons

Health Checks

### Why

Debugging distributed systems without observability is impractical.

---

# ADR-017
## AI is an Assistant, Not the Core Engine

### Decision

AI enhances user productivity but never directly executes workflows.

The workflow engine remains deterministic.

AI assists with:

- Workflow generation
- Workflow explanation
- Failure analysis
- Documentation
- Test payload generation

### Why

Deterministic execution is essential for reliability and reproducibility.

---

# ADR-018
## MVP Before Platform Expansion

### Decision

Prioritize a polished MVP over implementing every possible connector and feature.

### Why

A focused, reliable platform demonstrates stronger engineering than a broad but incomplete feature set.

Future capabilities such as additional connectors, multi-tenancy, plugin marketplaces, and enterprise features will be added only after the core platform is stable.

---

# Engineering Principles

Every future architectural decision should satisfy these principles:

- Reliability over novelty.
- Simplicity over unnecessary complexity.
- Extensibility over rigid implementations.
- Observability over assumptions.
- Security by default.
- AI assists engineering rather than replacing deterministic system behavior.
- Every feature should justify its operational cost.
- Design for maintainability first, then optimization.

---

# Summary

The architectural decisions documented here establish the foundation for FlowForge as a production-inspired automation platform rather than a hackathon prototype.

By deliberately choosing proven patterns such as Event-Driven Architecture, Transactional Outbox, Idempotency, Retry Policies, Dead Letter Queues, Redis-based Distributed Locking, and Spring AI for intelligent assistance, the platform balances modern engineering practices with the practical constraints of a ten-day implementation.

These ADRs will guide implementation, simplify future evolution, and provide a clear rationale for every major technology and design decision throughout the lifecycle of the project.