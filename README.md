# 🚀 FlowForge

> **Build. Execute. Observe. Improve.**

An AI-powered business automation platform that enables users to create, execute, monitor, and optimize workflows using **Natural Language**, **Visual Workflow Builder**, and a **production-grade event-driven execution engine**.

Built with **Spring Boot**, **Spring AI**, **Kafka**, **Redis**, **PostgreSQL**, and **React**.

---

## ✨ Why FlowForge?

Modern automation platforms either focus on no-code workflows or developer-centric orchestration.

FlowForge combines both.

Describe your business process in plain English, let AI generate the workflow, execute it reliably using an event-driven engine, monitor it in real time, recover automatically from failures, and continuously improve using AI-powered insights.

---

## 🎬 Demo Flow

```
Natural Language Prompt
        │
        ▼
Spring AI generates Workflow
        │
        ▼
Visual Workflow Builder
        │
        ▼
Execute Workflow
        │
        ▼
Kafka Event Pipeline
        │
        ▼
Worker Execution
        │
        ▼
Failure → Retry → DLQ
        │
        ▼
AI Root Cause Analysis
        │
        ▼
Replay Execution
        │
        ▼
Success ✅
```

---

# 🌟 Core Features

## 🤖 AI Workflow Builder

- Generate workflows using natural language
- AI workflow explanation
- AI optimization suggestions
- AI failure analysis
- AI test payload generation

---

## 🔄 Workflow Engine

- DAG Validation
- Parallel Execution
- Retry Policies
- Replay Support
- Workflow Versioning
- Execution Timeline

---

## ⚡ Production Patterns

- Event-Driven Architecture
- Kafka
- Transactional Outbox
- Idempotency
- Distributed Locking
- Redis Caching
- Dead Letter Queue
- Exponential Backoff
- Rate Limiting

---

## 🔌 Connector Framework

MVP Connectors

- HTTP Request
- Email
- Webhook
- Delay
- AI Task
- Logger
- Slack / Discord

---

## 📊 Monitoring Dashboard

- Live Execution Timeline
- Workflow History
- Retry Analytics
- Failed Executions
- Replay
- Worker Status
- Execution Metrics

---

# 🏗️ Architecture

```
React UI
      │
 REST API
      │
Spring Boot
(Modular Monolith)
      │
Workflow Engine
      │
Transactional Outbox
      │
Kafka
      │
Workers
      │
Execution Events
      │
Dashboard
```

---

# 🛠 Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring AI
- Spring Data JPA
- Spring Validation

## Frontend

- React
- TypeScript
- React Flow
- Tailwind CSS
- TanStack Query
- React Router

## Database

- PostgreSQL

## Infrastructure

- Kafka
- Redis
- Docker Compose

## Documentation

- Swagger / OpenAPI
- Markdown
- PlantUML

---

# 📂 Project Structure

```
flowforge/

backend/
frontend/

docs/
    ARCHITECTURE.md
    DESIGN.md
    DATABASE.md
    API.md
    DEMO.md

docker/

README.md
```

---

# 🚀 Getting Started

### Clone Repository

```bash
git clone <repository-url>
```

### Start Infrastructure

```bash
docker compose up -d
```

### Start Backend

```bash
./mvnw spring-boot:run
```

### Start Frontend

```bash
npm install
npm run dev
```

---

# 🎯 MVP Scope

- Authentication
- Workflow Builder
- AI Workflow Generator
- Kafka Execution Engine
- Retry & DLQ
- Monitoring Dashboard
- Replay
- Docker Deployment

---

# 📈 Future Roadmap

- Marketplace
- Multi-Tenant Organizations
- Plugin SDK
- Kubernetes Deployment
- Additional Connectors
- AI Workflow Optimization
- Predictive Failure Detection

---

# 🏆 Engineering Highlights

- Modular Monolith Architecture
- Event-Driven Execution
- Production-Grade Reliability Patterns
- AI-Native Workflow Creation
- Extensible Connector Framework
- Clean Architecture & Domain-Driven Design

---

# 👥 Team

**Meet Sanghani**  
Backend • Architecture • AI • Distributed Systems

**Team Member**  
Frontend • React • UI/UX • Dashboard

---

# 📄 License

This project was built as part of the Coding Shuttle Build-A-Thon 2026 and will be made public after the hackathon concludes.

## Local Development

### Services

| Service | Port |
|----------|------|
| Spring Boot | 8080 |
| PostgreSQL | 5433 |
| Redis | 6379 |
| Kafka | 9092 |
| Kafka UI | 8081 |
