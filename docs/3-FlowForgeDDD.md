# FlowForge
# Domain-Driven Design (DDD) & Domain Model

**Version:** 1.0

**Status:** Final

**Authors:** Meet Sanghani & Team

---

# Purpose

This document defines the business domain of FlowForge using Domain-Driven Design (DDD) principles.

Its purpose is to establish a common language between engineering, product, and future contributors while providing a blueprint for implementation.

This document identifies:

- Core Domain
- Subdomains
- Bounded Contexts
- Aggregates
- Entities
- Value Objects
- Domain Services
- Domain Events
- Repository Contracts
- Workflow Lifecycle
- Future Service Boundaries

---

# Ubiquitous Language

The following terminology should be used consistently throughout the codebase and documentation.

| Term | Meaning |
|------|---------|
| Workflow | A reusable automation definition composed of connected nodes. |
| Workflow Version | An immutable published version of a workflow. |
| Workflow Execution | A runtime instance of a workflow execution. |
| Node | A single executable step within a workflow. |
| Edge | A dependency between two workflow nodes. |
| Connector | An implementation that interacts with an external system or performs a specific task. |
| Execution Context | Runtime data shared between workflow nodes. |
| Trigger | An event that starts a workflow. |
| Worker | A background processor responsible for executing workflow nodes. |
| Execution Event | A domain event emitted during workflow execution. |
| Replay | Re-execution of a previously failed workflow. |
| Template | A predefined workflow that users can customize. |

---

# Domain Classification

## Core Domain

The Workflow Orchestration Engine.

This is the heart of FlowForge and the primary differentiator of the platform.

Responsibilities:

- Workflow validation
- Dependency resolution
- Execution orchestration
- Retry handling
- Scheduling
- State transitions

---

## Supporting Domains

### Authentication

Responsible for:

- Users
- Roles
- Permissions
- Sessions

---

### AI Assistant

Responsible for:

- Workflow generation
- Prompt processing
- Workflow optimization
- Failure explanation
- Documentation generation

---

### Connector Framework

Responsible for:

- External integrations
- Connector execution
- Connector configuration
- Connector validation

---

### Monitoring & Observability

Responsible for:

- Metrics
- Logs
- Execution timeline
- Health checks
- Retry history

---

# Bounded Contexts

## Identity Context

Owns:

- User
- Role
- Permission
- Authentication

Independent from workflow execution.

---

## Workflow Context

Owns:

- Workflow
- Version
- Nodes
- Edges
- Templates

This is the design-time context.

---

## Execution Context

Owns:

- Executions
- Execution State
- Retry Logic
- Scheduling
- Replay

This is the runtime context.

---

## Connector Context

Owns:

- Connector Definitions
- Connector Execution
- Connector Configuration
- Connector Registry

---

## AI Context

Owns:

- Prompt Templates
- AI Requests
- AI Responses
- Structured Output
- Recommendations

---

## Monitoring Context

Owns:

- Metrics
- Logs
- Execution History
- Worker Status

---

# Aggregate Roots

## User

Root of Identity Context.

Owns

- Roles
- Profile
- Credentials

---

## Workflow

Aggregate Root

Owns

- Metadata
- Versions
- Nodes
- Edges

Rules

A workflow cannot be published unless:

- DAG is valid
- Trigger exists
- Every node passes validation

---

## Workflow Version

Immutable after publishing.

Rules

Published versions cannot be modified.

New versions must be created.

---

## Workflow Execution

Owns

- Runtime state
- Execution context
- Retry count
- Current node
- Failure reason

Rules

Execution state changes must follow the state machine.

---

## Connector

Owns

- Configuration
- Validation rules
- Capabilities

---

# Entities

## User

Fields

- id
- name
- email
- password
- role
- status
- createdAt

---

## Workflow

Fields

- id
- name
- description
- ownerId
- status
- latestVersion
- createdAt

---

## WorkflowVersion

Fields

- id
- workflowId
- versionNumber
- definition
- checksum
- publishedAt

---

## Node

Fields

- id
- type
- configuration
- position
- timeout
- retryPolicy

---

## Edge

Fields

- source
- destination
- condition

---

## WorkflowExecution

Fields

- id
- workflowVersionId
- state
- startedAt
- completedAt
- executionContext
- retryCount

---

## Connector

Fields

- id
- type
- configuration
- enabled

---

## Worker

Fields

- id
- type
- status
- heartbeat

---

# Value Objects

RetryPolicy

Contains

- maxRetries
- delay
- exponentialBackoff

Immutable.

---

ExecutionContext

Contains runtime variables.

Immutable snapshots.

---

NodeConfiguration

Contains connector-specific configuration.

---

PromptDefinition

Contains

- systemPrompt
- userPrompt
- expectedSchema

---

# Domain Services

## WorkflowValidationService

Responsibilities

- Validate DAG
- Detect cycles
- Verify connectors
- Validate configuration

---

## WorkflowExecutionService

Responsibilities

- Start execution
- Resume execution
- Complete execution

---

## RetryService

Responsibilities

- Retry calculation
- Backoff computation
- Retry scheduling

---

## ConnectorExecutionService

Responsibilities

- Resolve connector
- Execute connector
- Capture response

---

## AIWorkflowGenerationService

Responsibilities

- Prompt AI
- Parse structured output
- Validate workflow

---

## AIAnalysisService

Responsibilities

- Analyze failures
- Suggest improvements
- Generate explanations

---

# Domain Events

WorkflowCreated

WorkflowUpdated

WorkflowPublished

WorkflowTriggered

ExecutionStarted

NodeExecutionStarted

NodeExecutionCompleted

NodeExecutionFailed

RetryScheduled

ExecutionCompleted

ExecutionFailed

ExecutionReplayed

ConnectorExecuted

AIWorkflowGenerated

FailureAnalyzed

WorkflowOptimized

These events become Kafka messages in the implementation.

---

# Workflow State Machine

Draft

↓

Validated

↓

Published

↓

Triggered

↓

Queued

↓

Executing

↓

Completed

OR

Failed

↓

Retry Scheduled

↓

Executing

↓

Completed

OR

Dead Letter Queue

↓

Replay

---

# Execution State Machine

Pending

↓

Running

↓

Waiting

↓

Retrying

↓

Succeeded

OR

Failed

↓

Dead Letter Queue

↓

Replayed

---

# Repository Contracts

WorkflowRepository

WorkflowVersionRepository

ExecutionRepository

ConnectorRepository

UserRepository

WorkerRepository

Repositories expose domain operations rather than persistence details.

---

# Module Structure

flowforge

├── identity

├── workflow

├── execution

├── connector

├── ai

├── monitoring

├── shared

Each module owns its models, services, repositories, events, and APIs.

---

# Future Microservice Boundaries

The modular monolith is intentionally designed so each bounded context can evolve into an independent service.

Potential future services:

- Identity Service
- Workflow Service
- Execution Service
- Connector Service
- AI Service
- Monitoring Service
- Notification Service

This minimizes future migration effort while keeping the MVP operationally simple.

---

# Design Principles

- Aggregates enforce business invariants.
- Entities have identity and lifecycle.
- Value Objects are immutable.
- Domain Services contain business behavior that does not naturally belong to an entity.
- Events represent meaningful business occurrences.
- Modules communicate through events rather than direct coupling wherever practical.
- Business terminology remains consistent across code, APIs, documentation, and architecture diagrams.

---

# Summary

The Domain-Driven Design model establishes FlowForge as a platform centered around workflow orchestration rather than CRUD operations.

By organizing the system into clear bounded contexts and aggregate roots, the design creates strong separation of concerns while preserving flexibility for future evolution. This structure enables the MVP to remain a modular monolith today while providing a clear migration path toward independently deployable services as the platform grows.

This domain model forms the foundation for the database schema, event contracts, API design, package structure, and future architectural evolution of FlowForge.