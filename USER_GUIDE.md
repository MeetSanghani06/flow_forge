# FlowForge — User Guide & Local Setup

## 1. Overview

FlowForge is a visual workflow orchestration platform for designing, versioning, publishing, executing, and monitoring workflow DAGs.

The application provides:

* Visual DAG/workflow builder
* Multiple workflow versions
* Version selection
* Version creation
* Version cloning
* Version publishing
* Graph validation
* Workflow execution
* Live execution status polling
* Node-level execution tracking
* Execution input/output inspection
* Execution history lookup
* Conditional workflow edges
* AI-powered workflow nodes
* AI-powered workflow generation from natural-language prompts
* Extensible connector architecture
* PostgreSQL persistence
* Redis integration
* Kafka-based event-driven execution infrastructure
* Transactional/outbox-oriented reliability architecture
* Retry/DLQ-oriented execution architecture
* Rate limiting and distributed-system patterns

The project is currently intended to be run locally because a public deployment was not prepared for the submission.

---

# 2. Technology Stack

## Frontend

* React
* TypeScript
* TanStack Router
* React Flow / `@xyflow/react`
* Tailwind CSS
* shadcn-style UI components
* TanStack Query
* Vite

## Backend

* Java
* Spring Boot
* Spring Data JPA
* Spring Security
* Spring AI
* PostgreSQL
* Redis
* Apache Kafka
* Flyway

## AI

FlowForge uses Spring AI for AI-powered workflow functionality.

The default development configuration uses:

```text
gpt-4.1-mini
```

An OpenAI API key is required to execute AI functionality.

---

# 3. Prerequisites

Install the following before starting FlowForge:

* Git
* Java/JDK compatible with the project
* Maven, or use the Maven wrapper if included
* Node.js
* npm
* Docker Desktop
* Docker Compose
* PostgreSQL client tools are optional
* Postman is recommended for testing backend-only APIs

Verify the installations:

```bash
java -version
mvn -version
node -version
npm -version
docker --version
docker compose version
```

---

# 4. Project Structure

The repository contains the two primary application components:

```text
FlowForge/
├── backend/
│   ├── pom.xml
│   └── src/
│
├── frontend/
│   ├── package.json
│   └── src/
│
└── ...
```

If the repository uses a different directory name, use the corresponding backend/frontend directories.

---

# 5. Start Infrastructure

FlowForge requires PostgreSQL, Kafka and Redis for the local environment.

Start the infrastructure containers from the directory containing the project's Docker Compose file:

```bash
docker compose up -d
```

Check that the containers are running:

```bash
docker compose ps
```

Expected infrastructure includes:

```text
PostgreSQL
Kafka
Kafka UI
Redis
```

The local PostgreSQL configuration used during development was:

```text
Host: localhost
Port: 5433
Database: flowforge
Username: flowforge
Password: flowforge
```

The PostgreSQL container itself exposes its normal PostgreSQL port internally; the host development mapping uses port `5433`.

Kafka is available to the local Spring Boot application through:

```text
localhost:9092
```

Redis uses:

```text
localhost:6379
```

---

# 6. Backend Configuration

The backend reads configuration from environment variables.

Important variables include:

```bash
DB_HOST=localhost
DB_PORT=5433
DB_NAME=flowforge
DB_USERNAME=flowforge
DB_PASSWORD=flowforge

REDIS_HOST=localhost
REDIS_PORT=6379

KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

For AI functionality:

```bash
OPENAI_API_KEY=<your-openai-api-key>
```

Optional model configuration:

```bash
OPENAI_MODEL=gpt-4.1-mini
```

Do not commit a real OpenAI API key to Git.

For local development, configure the variable in the shell, IDE run configuration, or local environment file according to the project's existing configuration strategy.

Example:

```bash
export OPENAI_API_KEY="sk-..."
```

---

# 7. Start the Backend

Open a terminal in the backend directory:

```bash
cd backend
```

If the Maven wrapper is available:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

Otherwise:

```bash
mvn spring-boot:run
```

The backend should become available at:

```text
http://localhost:8080
```

The frontend is configured to use this backend by default.

The application's database migrations are handled through Flyway.

---

# 8. Start the Frontend

Open another terminal:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The application should be available at:

```text
http://localhost:5173
```

The frontend uses:

```text
VITE_API_BASE_URL
```

to determine the backend URL.

If this variable is not supplied, the development configuration defaults to:

```text
http://localhost:8080
```

---

# 9. Recommended Startup Order

For the smoothest local demonstration:

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
npm run dev
```

Then open:

```text
http://localhost:5173
```

---

# 10. First-Time Usage

## Step 1 — Register / Sign In

Open:

```text
http://localhost:5173
```

Create an account if required by the local backend and sign in.

FlowForge requires authentication before accessing workflows.

---

# 11. Create a Workspace

Create/select a workspace from the application.

Workflows belong to a workspace, so a workspace must be available before creating workflows.

---

# 12. Create a Workflow

From the workflow dashboard:

1. Select the workspace.
2. Click **Create Workflow**.
3. Enter a workflow name.
4. Optionally provide a description.
5. Create the workflow.
6. Open the workflow builder.

---

# 13. Build a Workflow DAG

The workflow builder provides a visual React Flow canvas.

Use the node palette to add nodes.

Typical workflow structure:

```text
Trigger
   |
   v
HTTP Request
   |
   v
AI Prompt
   |
   v
Transform / Action
```

Nodes can be positioned visually and connected using edges.

The graph is saved as a versioned workflow definition.

---

# 14. Configure Nodes

Select a node on the canvas.

The node inspector allows configuration of properties such as:

* Node name
* Node type
* Node key
* Configuration
* Connector information where applicable

The graph mapper converts the React Flow representation into the backend workflow graph representation.

Edges represent dependencies between nodes.

Conditional edges can be used where supported.

---

# 15. Save a Workflow

Click:

**Save**

FlowForge validates the graph before saving.

Typical validation requirements include:

* Valid workflow nodes
* Valid edges
* Correct node references
* Valid workflow structure
* Trigger constraints

If validation succeeds, the workflow graph is persisted against the selected workflow version.

---

# 16. Workflow Versions

FlowForge treats workflow definitions as versioned artifacts.

The version selector in the builder allows switching between versions.

For example:

```text
v1 · PUBLISHED
v2 · DRAFT
v3 · DRAFT
```

This allows experimentation without modifying an already published version.

---

# 17. Create a New Version

When a workflow already has versions, use:

**Create Version**

to create another workflow version.

After creation, the new version becomes selectable from the version selector.

A typical development flow is:

```text
v1
 |
 +--> Build
 |
 +--> Save
 |
 +--> Publish
 |
 +--> Create v2
 |
 +--> Modify v2
 |
 +--> Save
 |
 +--> Publish v2
```

---

# 18. Clone a Version

FlowForge also supports cloning an existing workflow version.

Cloning creates a new version based on an existing version.

The backend endpoint is:

```http
POST /api/v1/workspaces/{workspaceId}/workflows/{workflowId}/versions/{versionNumber}/clone
```

Example:

```http
POST http://localhost:8080/api/v1/workspaces/<workspaceId>/workflows/<workflowId>/versions/7/clone
```

The cloned version is returned as a new, unpublished version.

This is useful when you want to experiment with an existing workflow without changing the source version.

Recommended workflow:

```text
Published v7
     |
     | Clone
     v
Draft v8
     |
     +--> Modify
     |
     +--> Save
     |
     +--> Test
     |
     +--> Publish
```

---

# 19. Publish a Version

When the workflow is ready, click:

**Publish**

Publishing makes that workflow version available as the active executable version.

A recommended lifecycle is:

```text
DRAFT
  ↓
SAVE
  ↓
TEST
  ↓
PUBLISH
```

Do not treat an unvalidated draft as a production-ready workflow.

---

# 20. Execute a Workflow

Click:

**Execute**

The execution dialog allows providing workflow input.

The backend creates an execution and processes the workflow graph.

The execution lifecycle can include:

```text
QUEUED
   ↓
RUNNING
   ↓
SUCCESS
```

or:

```text
QUEUED
   ↓
RUNNING
   ↓
FAILED
```

The frontend polls the execution state while it is running.

---

# 21. Execution Details

After execution starts, FlowForge exposes the execution detail page.

The page displays:

* Execution ID
* Execution status
* Workflow version
* Version number
* Start time
* Completion time
* Error information
* Input
* Output

The execution page automatically polls non-terminal executions.

Terminal states stop polling.

---

# 22. Node-Level Execution History

FlowForge also exposes execution information for individual workflow nodes.

The node execution API is:

```http
GET /api/v1/workflow-executions/{executionId}/nodes
```

Example:

```http
GET http://localhost:8080/api/v1/workflow-executions/<executionId>/nodes
```

Each node execution can contain:

```text
id
executionId
nodeId
nodeKey
nodeName
name
nodeType
type
status
attempt
sequence
startedAt
completedAt
input
output
errorMessage
```

This allows the execution to be inspected at node granularity rather than only at the workflow level.

For example:

```text
Execution
│
├── Trigger       SUCCESS
│   ├── input
│   └── output
│
├── AI Prompt     SUCCESS
│   ├── input
│   └── output
│
├── HTTP Request  FAILED
│   ├── input
│   └── error
│
└── Final Action  SKIPPED
```

This is particularly useful for debugging DAG executions.

---

# 23. Execution History

From the workflow builder, click:

**History**

The history page allows an execution ID to be entered and inspected.

FlowForge also remembers recent executions started from the current browser.

This is currently implemented as a local browser history because the backend does not expose a general execution-list endpoint in the current submission.

To inspect an execution:

1. Open the workflow.
2. Click **History**.
3. Enter the execution ID.
4. Click **Open**.
5. Inspect the execution and node-level results.

---

# 24. AI Node

FlowForge contains an AI node powered through Spring AI.

Before using the AI node, configure:

```bash
OPENAI_API_KEY=<your-key>
```

Optionally:

```bash
OPENAI_MODEL=gpt-4.1-mini
```

Restart the backend after changing environment variables.

The AI node can then be configured from the workflow builder.

For example:

```text
Trigger
   ↓
AI Prompt
   ↓
HTTP / Action Node
```

The AI node receives the workflow context/input and produces an output that can be consumed by downstream nodes.

---

# 25. AI Workflow Generation

FlowForge also contains backend functionality for generating workflows from natural-language prompts.

This can be demonstrated through the backend API/Postman even if the corresponding generation UI is not used for the final demonstration.

Conceptually, the flow is:

```text
Natural-language prompt
        ↓
Spring AI
        ↓
Workflow definition
        ↓
Workflow graph
        ↓
Save/version
        ↓
Visual builder
        ↓
Execute
```

A suitable demonstration prompt is:

```text
Create a workflow that receives an input message,
uses AI to summarize it, and then sends the result
to a downstream HTTP action.
```

The exact request body and endpoint should be taken from the project's current backend controller/API implementation or Postman collection supplied with the repository.

---

# 26. OpenAI Configuration for Demonstration

The evaluator does NOT need an OpenAI API key to inspect the core workflow-builder functionality.

An OpenAI API key is only required for features that actually call the OpenAI model, including:

* AI workflow nodes
* AI-powered workflow generation

For the AI demonstration:

```bash
export OPENAI_API_KEY="YOUR_OPENAI_API_KEY"
export OPENAI_MODEL="gpt-4.1-mini"
```

Then restart the backend:

```bash
./mvnw spring-boot:run
```

Never commit the key to the repository.

---

# 27. Connector Architecture

FlowForge is designed to support external connectors such as:

* GitHub
* LinkedIn
* HTTP APIs
* Other external services

Connector support is part of the extensible workflow architecture.

For the current submission, the primary demonstration should focus on the workflow engine and built-in workflow capabilities rather than requiring external OAuth credentials.

External connector demonstrations may require separate provider credentials and configuration.

---

# 28. Suggested 5-Minute Demo

For evaluators, the following sequence provides the clearest demonstration.

### 1. Open FlowForge

```text
http://localhost:5173
```

### 2. Open a workflow

Show the visual DAG.

### 3. Explain the graph

Point out:

* Trigger
* Nodes
* Edges
* Configuration
* Conditional execution where applicable

### 4. Show versioning

Demonstrate:

```text
v1 → Publish
v2 → Create
v2 → Modify
v2 → Save
v2 → Clone if required
```

### 5. Execute

Click:

```text
Execute
```

Provide sample input.

### 6. Show execution

Demonstrate:

```text
QUEUED → RUNNING → SUCCESS
```

### 7. Show node-level execution

Open the execution details and show that each node has its own execution state, input/output, timing and error information.

### 8. Show History

Click:

```text
History
```

Open the execution using its execution ID.

### 9. Demonstrate AI

If an OpenAI API key is configured:

```text
AI Node → Execute → inspect AI output
```

If time permits, demonstrate workflow generation from a natural-language prompt through the backend/Postman API.

---

# 29. Troubleshooting

## Backend cannot connect to PostgreSQL

Check:

```bash
docker compose ps
```

Verify PostgreSQL is running.

Check:

```text
DB_HOST=localhost
DB_PORT=5433
DB_NAME=flowforge
DB_USERNAME=flowforge
DB_PASSWORD=flowforge
```

If the database container was recreated, verify its credentials match the backend configuration.

---

## Kafka connection failure

Verify:

```bash
docker compose ps
```

and confirm Kafka is running.

Check:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

---

## Redis connection failure

Verify Redis is running and configured as:

```text
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## AI node fails

Verify:

```bash
echo $OPENAI_API_KEY
```

Then verify the backend model configuration:

```text
OPENAI_MODEL=gpt-4.1-mini
```

Restart the backend after setting the environment variable.

---

## Frontend cannot reach backend

Verify backend:

```text
http://localhost:8080
```

Then verify frontend configuration:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Restart the Vite development server after changing Vite environment variables.

---

## Database migration problem

FlowForge uses Flyway for database migrations.

If the database contains stale development migrations or a migration checksum mismatch, do not blindly modify migration files.

For a fresh evaluation environment, recreating the local development database/containers is generally preferable to manually modifying migration history.

---

# 30. Stopping the Application

Stop the frontend and backend with:

```text
Ctrl+C
```

Stop Docker infrastructure:

```bash
docker compose down
```

To remove local development volumes as well, use the project's documented volume cleanup procedure.

**Warning:** removing volumes can delete the local FlowForge database.

---

# 31. Evaluation Notes

The application is designed to demonstrate the complete workflow lifecycle:

```text
Design
  ↓
Validate
  ↓
Save
  ↓
Version
  ↓
Publish
  ↓
Execute
  ↓
Persist execution
  ↓
Track node executions
  ↓
Inspect output
  ↓
Debug through history
```

The backend workflow executor evaluates the graph and maintains execution context across nodes. Node execution state is persisted individually, allowing detailed execution inspection rather than exposing only a final workflow result.

---

# 32. Known Submission Limitations

The current submission is intentionally focused on the workflow orchestration engine and its core user experience.

Known limitations:

1. The project does not have a public hosted deployment for this submission.
2. Local execution is therefore required.
3. OpenAI-dependent features require the evaluator's own OpenAI API key.
4. External connector demonstrations may require provider-specific credentials.
5. The current History page uses execution-ID lookup and locally remembered browser executions because a general backend execution-list API is not currently exposed.
6. Connector configuration/OAuth flows are not the primary part of the submitted UI demonstration.

These limitations do not prevent evaluation of the core workflow engine, versioning, execution, monitoring, and AI functionality.

---

# 33. Recommended Evaluation Environment

For the most reliable evaluation:

```text
Docker Desktop
Java/JDK
Maven
Node.js + npm
Postman
OpenAI API key (only for AI features)
```

Start:

```bash
docker compose up -d
```

Then:

```bash
cd backend
./mvnw spring-boot:run
```

And:

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

---

# 34. Final Demonstration Checklist

* [ ] Docker infrastructure is running
* [ ] PostgreSQL is healthy
* [ ] Kafka is healthy
* [ ] Redis is healthy
* [ ] Backend starts successfully
* [ ] Frontend starts successfully
* [ ] User can register/sign in
* [ ] Workspace is available
* [ ] Workflow can be created
* [ ] Nodes can be added
* [ ] Nodes can be configured
* [ ] Nodes can be connected
* [ ] Graph can be saved
* [ ] Version selector works
* [ ] New version can be created
* [ ] Version can be cloned
* [ ] Version can be published
* [ ] Workflow can be executed
* [ ] Execution reaches SUCCESS/FAILED
* [ ] Execution input/output is visible
* [ ] Node executions are visible
* [ ] History page works
* [ ] AI node works with an OpenAI API key
* [ ] AI workflow generation can be demonstrated through the backend/Postman API if required
* [ ] No secrets are committed to Git

---

# 35. What to Submit

Recommended submission package:

```text
FlowForge/
│
├── backend/
├── frontend/
├── docker-compose.yml
├── README.md
├── USER_GUIDE.md
├── ARCHITECTURE.md
├── DATABASE.md
├── API.md
└── ...
```

The evaluator should start with:

```text
README.md
```

and then use:

```text
USER_GUIDE.md
```

for the complete local setup and demonstration instructions.

For AI functionality, the evaluator should configure their own:

```text
OPENAI_API_KEY
```

rather than expecting a shared API key to be included in the repository.

---

## FlowForge in one sentence

**FlowForge is a versioned, event-driven workflow orchestration platform that lets users visually design DAGs, execute them reliably, inspect every node's execution, and use AI to create or execute intelligent workflow steps.**
