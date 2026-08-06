# 🏗️ FlowForge Architecture Guide

> **Version:** 1.0  
> **Status:** Final  
> **Architecture Style:** Modular Monolith + Event-Driven Execution

---

# 1. Architecture Overview

FlowForge is built as a **Modular Monolith** with **asynchronous event-driven processing**.

The platform separates business capabilities into independent modules while sharing a single deployable Spring Boot application. Long-running tasks are executed asynchronously through Kafka workers.

## Core Principles

- Modular by Design
- Event-Driven Execution
- AI-Assisted Development
- Production-Grade Reliability
- High Observability
- Future Microservice Ready

---

# 2. High-Level Architecture

```text
                           +----------------------+
                           |      React UI        |
                           +----------+-----------+
                                      |
                               REST APIs
                                      |
                                      ▼
+--------------------------------------------------------------------+
|                    FlowForge Backend (Spring Boot)                  |
|--------------------------------------------------------------------|
| Identity | Workflow | Execution | AI | Connector | Monitoring       |
+--------------------------------------------------------------------+
                                      |
                            Transactional Outbox
                                      |
                                      ▼
                                  Apache Kafka
                                      |
         +--------------+-------------+-------------+
         |              |             |             |
         ▼              ▼             ▼             ▼
    HTTP Worker    Email Worker   AI Worker   Delay Worker
         |              |             |             |
         +--------------+-------------+-------------+
                                      |
                                      ▼
                            Execution Event Stream
                                      |
                                      ▼
                           Monitoring Dashboard

Infrastructure:
- PostgreSQL
- Redis
- Docker Compose
```

---

# 3. Backend Architecture

```text
com.flowforge

├── identity
│   ├── controller
│   ├── service
│   ├── entity
│   ├── repository
│   ├── dto
│   ├── mapper
│   ├── config
│   └── security
│
├── workflow
│   ├── controller
│   ├── service
│   ├── entity
│   ├── repository
│   ├── validator
│   ├── dto
│   ├── mapper
│   └── event
│
├── execution
│   ├── controller
│   ├── service
│   ├── engine
│   ├── worker
│   ├── scheduler
│   ├── retry
│   ├── replay
│   └── event
│
├── connector
│   ├── http
│   ├── email
│   ├── webhook
│   ├── delay
│   ├── ai
│   ├── logger
│   ├── factory
│   └── registry
│
├── ai
│   ├── prompt
│   ├── service
│   ├── parser
│   ├── validator
│   └── dto
│
├── monitoring
│   ├── metrics
│   ├── dashboard
│   ├── health
│   └── analytics
│
├── infrastructure
│   ├── kafka
│   ├── redis
│   ├── postgres
│   ├── outbox
│   ├── scheduler
│   └── config
│
└── shared
    ├── exception
    ├── util
    ├── constants
    ├── enums
    └── common
```

---

# 4. Module Responsibilities

## Identity

- Authentication
- Authorization
- JWT
- User Management

---

## Workflow

- Workflow CRUD
- Versioning
- Validation
- Publishing

---

## Execution

- DAG Execution
- Retry
- Replay
- State Machine
- Scheduling

---

## Connector

- External Integrations
- Connector Registry
- Connector Factory
- Connector Execution

---

## AI

- Workflow Generation
- AI Chat
- Failure Analysis
- Optimization

---

## Monitoring

- Metrics
- Timeline
- Dashboard
- Analytics

---

# 5. Request Lifecycle

## Create Workflow

```text
React

↓

POST /workflows/ai

↓

Authentication

↓

Spring AI

↓

Workflow JSON

↓

Validator

↓

Workflow Service

↓

PostgreSQL

↓

Response
```

---

## Execute Workflow

```text
React

↓

POST /executions

↓

Execution Service

↓

Create Execution

↓

Transactional Outbox

↓

Kafka

↓

Worker

↓

Connector

↓

Execution Event

↓

Dashboard
```

---

# 6. Workflow State Machine

```text
Draft
   │
Validated
   │
Published
   │
Triggered
   │
Queued
   │
Running
   │
Completed
```

Failure Path

```text
Running

↓

Retry

↓

Retry

↓

Retry

↓

DLQ

↓

Replay

↓

Completed
```

---

# 7. Connector Architecture

Every connector implements:

```java
Connector

validate()

execute()

supportsRetry()

getConnectorType()
```

MVP Connectors

- HTTP
- Email
- Delay
- AI
- Logger
- Webhook
- Slack

---

# 8. Kafka Architecture

## Topics

```text
workflow.triggered

workflow.started

workflow.node.execute

workflow.node.completed

workflow.node.failed

workflow.retry

workflow.completed

workflow.failed

workflow.replay

workflow.dlq
```

---

# 9. Outbox Pattern

Business Transaction

↓

Workflow Saved

↓

Outbox Event Saved

↓

Transaction Commit

↓

Outbox Publisher

↓

Kafka

This guarantees reliable event delivery without dual-write issues.

---

# 10. Redis Usage

| Feature | Purpose |
|----------|---------|
| Workflow Cache | Faster workflow retrieval |
| Distributed Lock | Prevent duplicate execution |
| Rate Limiting | Protect APIs |
| Execution Cache | Active execution metadata |

---

# 11. AI Flow

```text
User Prompt

↓

Spring AI

↓

Structured JSON

↓

Workflow Validator

↓

Workflow Builder

↓

Save Workflow

↓

Visual Builder
```

AI also powers:

- Failure Analysis
- Workflow Explanation
- Optimization Suggestions
- Test Payload Generation

---

# 12. Reliability Patterns

- Transactional Outbox
- Idempotent Consumers
- Exponential Backoff
- Dead Letter Queue
- Distributed Locking
- Optimistic Locking
- Graceful Error Handling

---

# 13. Security

Authentication

- JWT Access Token
- Refresh Token

Authorization

- Role-Based Access Control

Protection

- Bean Validation
- Rate Limiting
- Secure Environment Variables
- Password Encryption (BCrypt)

---

# 14. Observability

Captured Metrics

- Workflow Executions
- Success Rate
- Failure Rate
- Retry Count
- Average Execution Time
- Active Workers
- Connector Performance

Logs

- Correlation ID
- Execution ID
- Workflow ID
- Node ID

---

# 15. Deployment

Docker Compose Services

```text
Frontend

Backend

Kafka

PostgreSQL

Redis
```

Single-command startup:

```bash
docker compose up
```

---

# 16. Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring AI
- Flyway

## Frontend

- React
- TypeScript
- React Flow
- Tailwind CSS
- TanStack Query

## Infrastructure

- Kafka
- PostgreSQL
- Redis
- Docker Compose

---

# 17. Future Evolution

The modular architecture allows seamless extraction into independent services:

- Identity Service
- Workflow Service
- Execution Service
- Connector Service
- AI Service
- Monitoring Service

No major refactoring of domain logic should be required.

---

# 18. Architecture Summary

FlowForge adopts a pragmatic architecture that balances delivery speed with production-grade engineering practices. The modular monolith keeps development simple for the hackathon while the event-driven execution engine showcases advanced backend concepts such as asynchronous processing, reliable messaging, idempotency, retries, and replay.

This architecture is intentionally designed to scale beyond the hackathon and serve as a strong portfolio project demonstrating modern Java, Spring Boot, distributed systems, and AI integration.