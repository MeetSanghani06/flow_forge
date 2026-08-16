# FlowForge

> **Production-grade, AI-ready workflow orchestration platform built with Spring Boot, Kafka, PostgreSQL, Redis, React, and Spring AI.**

FlowForge is an **Airflow-inspired workflow orchestration platform** that allows users to visually design DAG-based workflows, version them, publish immutable versions, execute workflows asynchronously, monitor executions, and inspect individual node executions.

The project was designed beyond a basic CRUD workflow builder, with a strong focus on **event-driven architecture, reliability, observability, concurrency, idempotency, versioning, retries, dead-letter handling, caching, and production-grade backend patterns**.

---

## 1. What Problem Does FlowForge Solve?

Modern applications frequently need to orchestrate multi-step processes:

```text
Trigger
   ↓
Fetch Data
   ↓
Transform Data
   ↓
AI Processing
   ↓
Store Result
   ↓
Notify / Continue
```

Hard-coding these workflows creates tightly coupled systems that are difficult to modify, retry, monitor, or version.

FlowForge provides a visual orchestration layer where workflows can be modeled as **DAGs (Directed Acyclic Graphs)** and executed asynchronously through an event-driven backend.

### Core capabilities

* Visual DAG/workflow builder
* Multiple workflow versions
* Version selection
* Version cloning
* Save workflow graphs
* Publish workflow versions
* Execute workflows
* Live execution status polling
* Per-node execution tracking
* Input/output inspection
* Retry handling
* Kafka-based asynchronous processing
* Transactional Outbox pattern
* Idempotent event processing
* Dead Letter Queue support
* Redis caching
* Rate limiting
* Distributed locking where required
* PostgreSQL persistence
* Flyway database migrations
* Spring AI integration
* Swagger/OpenAPI documentation
* React + React Flow based workflow editor

---

# 2. Architecture

```text
                         ┌─────────────────────────┐
                         │       React UI           │
                         │                         │
                         │ React + TypeScript      │
                         │ TanStack Router         │
                         │ React Flow              │
                         │ Tailwind / shadcn UI    │
                         └────────────┬────────────┘
                                      │ REST
                                      ▼
                         ┌─────────────────────────┐
                         │     Spring Boot API     │
                         │                         │
                         │ Workflow Management     │
                         │ Versioning              │
                         │ Graph Management        │
                         │ Execution API           │
                         │ AI Integration          │
                         └────────────┬────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
             ┌────────────┐    ┌────────────┐    ┌────────────┐
             │ PostgreSQL │    │   Redis    │    │   Kafka    │
             │            │    │            │    │            │
             │ Workflows  │    │ Cache      │    │ Events     │
             │ Versions   │    │ Rate Limit │    │ Commands   │
             │ Executions │    │ Locks      │    │ DLQ        │
             └────────────┘    └────────────┘    └─────┬──────┘
                                                       │
                                                       ▼
                                            ┌────────────────────┐
                                            │ Workflow Workers   │
                                            │                    │
                                            │ DAG execution      │
                                            │ Node execution     │
                                            │ Retry handling     │
                                            │ Idempotency        │
                                            └─────────┬──────────┘
                                                      │
                                                      ▼
                                            ┌────────────────────┐
                                            │     Spring AI      │
                                            │                    │
                                            │ OpenAI integration │
                                            │ AI workflow nodes  │
                                            └────────────────────┘
```

---

# 3. Technology Stack

## Frontend

* React
* TypeScript
* TanStack Router
* React Flow / `@xyflow/react`
* Tailwind CSS
* shadcn-style UI components
* React Query
* Sonner notifications
* Lucide icons

## Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Spring Kafka
* Spring AI
* MapStruct
* Flyway
* PostgreSQL
* Redis

## Infrastructure

* Docker / Docker Compose
* Apache Kafka
* PostgreSQL
* Redis
* Kafka UI

---

# 4. Key Architecture Decisions

## 4.1 DAG-based workflow model

Workflows are represented as directed graphs rather than fixed sequences.

This allows FlowForge to support:

* Multiple node types
* Dependencies between nodes
* Branching workflows
* Future parallel execution
* Extensible node implementations
* Workflow validation before execution

The frontend uses React Flow for graph editing while the backend stores a normalized workflow graph representation.

---

## 4.2 Immutable workflow versions

A workflow is versioned instead of being directly overwritten.

Example:

```text
Workflow
│
├── v1 → Published
├── v2 → Published
├── v3 → Draft
└── v4 → Draft
```

This gives us:

* Reproducible executions
* Safe workflow evolution
* Rollback capability
* Auditability
* Historical execution traceability

Published versions can therefore be treated as immutable execution definitions.

---

## 4.3 Version cloning

Any existing workflow version can be cloned into a new version.

Example:

```text
v5
 │
 └── Clone
      ↓
     v6
```

This allows users to safely experiment with an existing workflow without modifying the original version.

---

## 4.4 Event-driven execution

Workflow execution is designed around asynchronous events.

Instead of making the API synchronously execute an entire DAG:

```text
POST /execute
       │
       ▼
 API waits for entire workflow
```

FlowForge follows an event-driven approach:

```text
POST /execute
       │
       ▼
Create execution
       │
       ▼
Publish execution event
       │
       ▼
Kafka
       │
       ▼
Worker
       │
       ▼
Execute DAG
```

This improves scalability and prevents long-running workflow execution from blocking API requests.

---

# 5. Production-Grade Engineering Features

FlowForge was intentionally designed with production-grade distributed-system patterns.

## Transactional Outbox

Database state changes and outgoing events are coordinated through an **Outbox pattern**, reducing the risk of:

```text
Database updated
       +
Kafka publish failed
       =
Inconsistent system
```

Instead, events can be persisted transactionally and published reliably.

---

## Idempotency

Kafka and distributed systems can deliver messages more than once.

FlowForge therefore considers idempotent processing so that duplicate messages do not accidentally execute the same operation multiple times.

```text
Event
  ↓
Idempotency check
  ↓
Already processed? ── Yes ──> Ignore
  │
  No
  ↓
Process
  ↓
Mark processed
```

---

## Retry handling

Transient failures should not immediately fail an entire workflow.

The architecture supports retry-oriented execution:

```text
Node
 ↓
Failure
 ↓
Retry
 ↓
Failure
 ↓
Retry
 ↓
Failure
 ↓
DLQ
```

---

## Dead Letter Queue

Messages that cannot be successfully processed after retries can be routed to a **Dead Letter Queue (DLQ)** for investigation and recovery.

This prevents poison messages from continuously blocking normal processing.

---

## Distributed locking

Distributed locks can be used around operations that require exclusive access, preventing multiple workers from simultaneously performing conflicting operations.

---

## Redis caching

Redis is used as the fast-access layer for operations where caching provides value.

This reduces unnecessary database load and improves latency.

---

## Rate limiting

Rate limiting protects expensive APIs and infrastructure from uncontrolled traffic.

This is particularly valuable for:

* Workflow execution APIs
* AI operations
* External integrations
* Expensive backend operations

---

## Optimistic / safe versioning

Workflow changes are version-aware, allowing workflow definitions to evolve without destroying previous executable definitions.

---

## Database migrations

Flyway is used for deterministic database schema management.

```text
Application startup
       ↓
Flyway
       ↓
Validate migrations
       ↓
Apply pending migrations
       ↓
Application starts
```

---

## Observability

The backend exposes Spring Boot Actuator endpoints for operational visibility.

Relevant endpoints include:

```text
/actuator/health
/actuator/info
/actuator/metrics
```

---

# 6. AI Capabilities

FlowForge includes **Spring AI** integration for AI-powered workflow nodes.

The AI node can be configured to communicate with OpenAI.

Example conceptual workflow:

```text
Trigger
   ↓
Fetch/Input
   ↓
AI Processing
   ↓
Transform
   ↓
Output
```

The OpenAI API key is intentionally **not committed to the repository**.

Set it locally before testing AI functionality:

```bash
export OPENAI_API_KEY="your-api-key"
```

Or configure it through the environment configuration used by the backend.

The default model configured for the project can be overridden using:

```bash
OPENAI_MODEL=gpt-4.1-mini
```

> AI functionality is optional for running the core workflow orchestration platform.

---

# 7. Local Development Setup

## Prerequisites

Install:

* Java
* Maven
* Node.js
* npm
* Docker
* Docker Compose

Recommended:

```text
Java 25
Node.js 20+
Docker Desktop
```

---

# 8. Start Infrastructure

From the backend/infrastructure directory, start the required services:

```bash
docker compose up -d
```

Expected infrastructure:

```text
PostgreSQL
Kafka
Kafka UI
Redis
```

Verify containers:

```bash
docker ps
```

Typical services:

```text
flowforge-postgres
flowforge-kafka
flowforge-kafka-ui
redis
```

---

# 9. PostgreSQL

The local development database is configured approximately as:

```text
Database:  flowforge
Username: flowforge
Password: flowforge
Host:     localhost
Port:     5433
```

The container itself exposes PostgreSQL on:

```text
5432
```

while the host maps it to:

```text
5433
```

---

# 10. Backend Setup

Navigate to the backend project:

```bash
cd backend
```

Set environment variables if required:

```bash
export DB_HOST=localhost
export DB_PORT=5433
export DB_NAME=flowforge
export DB_USERNAME=flowforge
export DB_PASSWORD=flowforge
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

For AI functionality:

```bash
export OPENAI_API_KEY="your-api-key"
export OPENAI_MODEL="gpt-4.1-mini"
```

Start Spring Boot:

```bash
./mvnw spring-boot:run
```

or:

```bash
mvn spring-boot:run
```

---

# 11. Swagger / OpenAPI

Once the backend is running, open:

**Swagger UI**

```text
http://localhost:8080/swagger-ui/index.html
```

**OpenAPI JSON**

```text
http://localhost:8080/v3/api-docs
```

Swagger is the easiest way to manually test the backend APIs without the frontend.

> If the backend is configured to run on a different port, replace `8080` accordingly.

---

# 12. Frontend Setup

Navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev -- --port 5173
```

The frontend will be available at:

```text
http://localhost:5173
```

---

# 13. Complete Startup Sequence

For a clean local run:

### Terminal 1 — Infrastructure

```bash
docker compose up -d
```

### Terminal 2 — Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Terminal 3 — Frontend

```bash
cd frontend
npm install
npm run dev -- --port 5173
```

Then open:

```text
http://localhost:5173
```

---

# 14. Recommended Demo Flow

The following flow demonstrates the majority of FlowForge's functionality.

## Step 1 — Create a workflow

Open:

```text
http://localhost:5173
```

Navigate to the workflow section and create a workflow.

Example:

```text
Customer Data Processing
```

---

## Step 2 — Create the first version

Open the workflow builder.

If the workflow does not have a version, select:

```text
Create first version
```

---

## Step 3 — Build the DAG

Use the node palette to add nodes.

Example:

```text
Trigger
   ↓
HTTP / Data Input
   ↓
Transform
   ↓
AI
   ↓
Output
```

Configure each node using the node inspector.

---

## Step 4 — Connect nodes

Connect nodes using React Flow.

Example:

```text
Trigger ──→ Transform ──→ AI ──→ Output
```

FlowForge validates the graph before saving.

---

## Step 5 — Save

Click:

```text
Save
```

The graph is converted from the React Flow representation into the backend workflow graph DTO.

---

## Step 6 — Create another version

Use:

```text
New Version
```

to create another version of the workflow.

The version selector allows switching between versions.

Example:

```text
v1
v2
v3
```

---

## Step 7 — Clone a version

Select the desired version and use the version cloning functionality.

Example:

```text
v2 → Clone → v3
```

The cloned version becomes an independent draft that can be modified without changing the original version.

---

## Step 8 — Publish

Save the workflow and select:

```text
Publish
```

The selected workflow version becomes the published executable definition.

---

## Step 9 — Execute

Use:

```text
Execute
```

to start a workflow execution.

The API creates the execution and the backend processes it asynchronously.

---

# 15. Execution Monitoring

The execution page provides:

* Execution ID
* Workflow version
* Version number
* Current execution status
* Start time
* Completion time
* Input
* Output
* Error information

Statuses include:

```text
QUEUED
RUNNING
SUCCESS
FAILED
```

While an execution is active, the UI polls for updated execution state.

---

# 16. Detailed Execution History

FlowForge provides a workflow History page.

Navigate to:

```text
/workspaces/{workspaceId}/workflows/{workflowId}/history
```

The history page supports execution-ID lookup.

It can also remember executions started from the current browser.

Detailed execution information includes node-level execution state such as:

```text
Node
├── Status
├── Attempt
├── Sequence
├── Started At
├── Completed At
├── Input
├── Output
└── Error
```

This makes it possible to understand **which individual node failed**, rather than only knowing that the complete workflow failed.

---

# 17. Testing AI Nodes

To test AI functionality, configure:

```bash
export OPENAI_API_KEY="your-api-key"
```

Optionally:

```bash
export OPENAI_MODEL="gpt-4.1-mini"
```

Restart the backend after setting the variables.

Then configure an AI node from the workflow builder and execute the workflow.

The execution page can be used to inspect the resulting node input/output.

---

# 18. Testing Through Postman / Swagger

The backend can also be tested independently from the frontend.

Recommended testing sequence:

```text
Create workflow
      ↓
Create workflow version
      ↓
Save workflow graph
      ↓
Publish version
      ↓
Execute workflow
      ↓
Get execution
      ↓
Get node executions
```

For AI workflow generation, if enabled in the backend, the corresponding API can also be called directly from Postman.

Example conceptual request:

```json
{
  "prompt": "Create a workflow that receives customer feedback, summarizes it using AI, classifies the sentiment, and stores the result."
}
```

The exact endpoint and request schema can be inspected from Swagger.

This provides a convenient way to demonstrate backend AI capabilities even when the corresponding UI functionality is not being used during the demo.

---

# 19. API Highlights

The platform exposes APIs for:

```text
Workspaces
    ↓
Workflows
    ↓
Workflow Versions
    ↓
Workflow Graph
    ↓
Workflow Execution
    ↓
Node Executions
```

Important capabilities include:

### Workflow management

```text
Create workflow
Get workflow
List workflows
```

### Version management

```text
Create version
List versions
Clone version
Publish version
```

### Graph management

```text
Get workflow graph
Save workflow graph
```

### Execution

```text
Start execution
Get execution
Get node executions
```

### AI

```text
Generate workflow from prompt
AI-powered workflow nodes
```

Use Swagger for the exact API contracts.

---

# 20. Why FlowForge Is More Than a CRUD Application

A key design goal was to avoid building another simple:

```text
Create
Read
Update
Delete
```

application.

FlowForge instead addresses real distributed workflow orchestration problems:

### Reliability

* Retries
* DLQ
* Idempotency
* Transactional Outbox

### Scalability

* Kafka-based asynchronous processing
* Worker-oriented execution
* Redis caching
* Rate limiting

### Consistency

* Versioned workflow definitions
* Immutable published versions
* Database transactions
* Idempotent event processing

### Operability

* Execution tracking
* Node-level execution history
* Error inspection
* Actuator metrics
* Swagger/OpenAPI

### Developer experience

* Visual DAG builder
* Version selector
* Version cloning
* Live execution status
* JSON input/output inspection
* AI workflow capabilities

---

# 21. Important Production-Grade Keywords

FlowForge intentionally incorporates concepts commonly used in real-world distributed systems:

```text
Microservices
Event-Driven Architecture
Apache Kafka
Transactional Outbox
Idempotency
Dead Letter Queue
Retry / Backoff
Distributed Locking
Rate Limiting
Redis Caching
DAG Orchestration
Asynchronous Processing
Workflow Versioning
Immutable Published Versions
Optimistic Concurrency
Database Transactions
Flyway Migrations
Observability
Actuator Metrics
OpenAPI / Swagger
Spring AI
AI-powered Workflow Automation
Fault Tolerance
Horizontal Scalability
Separation of Concerns
```

These are not merely buzzwords; they informed the architecture and implementation decisions of the platform.

---

# 22. Security & Configuration

Secrets must never be committed to source control.

Especially:

```text
OPENAI_API_KEY
Database credentials
Authentication secrets
External integration credentials
```

Use environment variables or a proper secrets manager in production.

For the hackathon/local setup, environment variables are sufficient.

---

# 23. Current Scope / Future Extensions

The current platform focuses on the core workflow orchestration experience.

Potential future extensions include:

* GitHub connector
* LinkedIn connector
* Slack connector
* Email connector
* More HTTP/API connectors
* OAuth-based connector authorization
* Rich workflow templates
* Workflow scheduling / cron
* Parallel DAG execution
* Conditional branching
* Workflow marketplace
* Advanced execution analytics
* Multi-tenant worker pools
* Kubernetes deployment
* Prometheus/Grafana dashboards
* Full AI workflow generation UI

The backend architecture is intentionally designed to allow these capabilities to be added without redesigning the entire platform.

---

# 24. Hackathon Demo — Recommended Story

For the final demonstration, focus on the following narrative:

> **"FlowForge is an AI-ready, event-driven workflow orchestration platform designed to bring production-grade reliability to visual workflow automation."**

Then demonstrate:

```text
1. Create workflow
        ↓
2. Add nodes
        ↓
3. Connect DAG
        ↓
4. Save
        ↓
5. Create / clone version
        ↓
6. Publish
        ↓
7. Execute
        ↓
8. Kafka processes execution asynchronously
        ↓
9. Monitor execution
        ↓
10. Inspect individual node executions
        ↓
11. Demonstrate AI node
        ↓
12. Show Swagger + architecture
```

This showcases both the **product experience** and the **engineering depth** behind it.

---

# 25. Project Highlights

### Product

* Visual workflow builder
* DAG-based automation
* Workflow versioning
* Version cloning
* Workflow publishing
* Execution monitoring
* Node-level execution history
* AI-powered workflow capabilities

### Engineering

* Spring Boot
* Kafka
* PostgreSQL
* Redis
* Spring AI
* React Flow
* Transactional Outbox
* Idempotency
* Retry handling
* DLQ
* Distributed locking
* Rate limiting
* Caching
* Database migrations
* Observability

### Architecture

```text
React UI
   ↓
REST API
   ↓
Spring Boot
   ↓
PostgreSQL + Redis
   ↓
Kafka
   ↓
Workflow Workers
   ↓
External Services / AI
```

---

# 26. Troubleshooting

## Backend cannot connect to PostgreSQL

Verify:

```bash
docker ps
```

Check that PostgreSQL is running and that the host port is:

```text
5433
```

Verify:

```text
DB_HOST=localhost
DB_PORT=5433
DB_NAME=flowforge
DB_USERNAME=flowforge
DB_PASSWORD=flowforge
```

---

## Kafka connection failure

Verify Kafka is running:

```bash
docker ps
```

and that:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

matches the local Docker configuration.

---

## Redis connection failure

Verify Redis:

```bash
docker ps
```

Expected local configuration:

```text
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## AI node does not work

Verify:

```bash
echo $OPENAI_API_KEY
```

If empty, configure:

```bash
export OPENAI_API_KEY="your-api-key"
```

Then restart the backend.

---

## Frontend is running on the wrong port

Start explicitly with:

```bash
npm run dev
```

Then open:

```text
http://localhost:5173
```

---

# 27. Final Local URLs

### FlowForge UI

```text
http://localhost:5173
```

### Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI specification

```text
http://localhost:8080/v3/api-docs
```

### Actuator health

```text
http://localhost:8080/actuator/health
```

### Kafka UI

```text
http://localhost:8081
```

> Kafka UI port may vary depending on the Docker Compose configuration.

---

# 28. Final Note

FlowForge was built with the goal of demonstrating not only **what a workflow automation product looks like**, but also **how a production-grade workflow execution engine can be architected**.

The combination of:

**DAG orchestration + versioning + asynchronous Kafka execution + transactional outbox + idempotency + retries + DLQ + Redis + distributed locking + rate limiting + Spring AI + node-level observability**

provides a foundation that can evolve from a hackathon prototype into a significantly larger workflow automation platform.

---

## 🚀 FlowForge

**Design workflows. Version safely. Execute asynchronously. Observe everything. Automate with AI.**
