# Automation Gateway

Automation Gateway is a Spring Boot backend that orchestrates document
processing, AI analysis, vector search, and downstream automation workflows.

It demonstrates:
- AI-driven document analysis (RAG)
- Microservice orchestration (Java ↔ Python)
- Event-driven automation with explicit fallback handling (n8n)
- Production-style Docker deployment
- Observable execution via logs (no UI required)

The Automation Gateway image is versioned (v1.0.0) to ensure reproducible demos.

---
## Quick Start (Prebuilt Demo – Recommended)

This project runs entirely via Docker Compose using **prebuilt images**.
No local builds required.

### Start the system
```bash
docker-compose -f docker-compose.demo.yml pull
docker-compose -f docker-compose.demo.yml up
```
Trigger AI-driven processing:
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Create an invoice entry for ACME Corp"
  }'
```
Verify automation (recommended):
```
docker-compose -f docker-compose.demo.yml logs -f automation-gateway
```
Expected behavior:
- AI analysis is performed
- An action type is derived
- A downstream automation workflow is triggered
- Execution is confirmed via logs
---

# What the System does
## End-to-End Flow
1. Client sends text via ```POST /api/documents```
2. Spring stores the document in H2 as `PROCESSING`
3. Spring calls the Python RAG Agent:
   ```http://rag-api:8000/api/query```
4. RAG Agent:
- Embeds text
- Retrieves similar context from Qdrant
- Runs AI reasoning
- Produces structured analysis (query, answer, documents)
5. Spring maps the RAG response into an `AiAnalysisResultDTO`,
   determines an `actionType` (`create_invoice_entry` or `create_support_ticket`),
   attaches the analysis JSON to the document, and updates the status
6. Spring calls the n8n webhook with:
- `documentId`
- `actionType`
- `analysis` payload
7. n8n executes the appropriate branch (invoice, support ticket, or future unknown)
8. Response is returned to the client

## Example Response Payload:
``` 
{
  "id": "5c4a8c9b-93bf-4e2f-9c60-7d9f5a0b1234",
  "status": "COMPLETED",
  "type": "RAG_ANSWER",
  "analysis": {
    "type": "RAG_ANSWER",
    "actionType": "create_invoice_entry",
    "fields": {
      "query": "Create an invoice entry for ACME Corp",
      "answer": "The invoice entry has been drafted for ACME Corp …",
      "documents": [ /* retrieved context */ ],
      "source": "rag-agent-service"
    }
  },
  "createdAt": "2025-01-01T10:15:30Z",
  "updatedAt": "2025-01-01T10:15:32Z"
}
```
---

## Additional Example Requests

The following examples demonstrate different AI-driven outcomes.

### Support Request (Automation Triggered)
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Customer reports they cannot log in to their account and needs assistance."
  }'
```
Expected behavior:
- RAG call fails or business logic decides this is a support case
- `actionType`: `create_support_ticket`

n8n creates a ClickUp task with:
- Title from `analysis.fields.query`
- Description from `analysis.fields.answer`
- Optional due date and assignee (configured in the ClickUp node)

- Execution visible in logs and ClickUp

### Knowledge Query (RAG only, no automation)
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Explain quantum computing in simple terms."
  }'
```
Expected behavior:
- AI analysis
- The demonstration logic may choose not to emit a separate automation
  action for pure knowledge queries (or reuse one of the existing types, depending
  on the current mapping)
- System correctly avoids over-automation

### Unsupported/Unknown Intent (Extension Scenario)
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Do something completely unrelated with no clear intent."
  }'
```
Expected behavior:
- A non-mapped `actionType` could be emitted
- n8n routes to the `unknown` branch
- Event is logged but no business logic action is executed

In the current demo code the Java service only emits the two
documented action types, but the n8n workflow is prepared for
safe extension.

---

### Direct automation checks (n8n webhook, prod-style)

In the demo stack, n8n exposes the automation webhook at:

```text
http://localhost:5678/webhook/ai-action
```

1) `create_invoice_entry` (invoice flow)
```
curl -X POST "http://localhost:5678/webhook/ai-action" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "demo-doc-1",
    "status": "COMPLETED",
    "actionType": "create_invoice_entry",
    "type": "RAG_ANSWER",
    "analysis": {
      "type": "RAG_ANSWER",
      "actionType": "create_invoice_entry",
      "fields": {
        "customer": "ACME GmbH",
        "amount": 199.99,
        "currency": "EUR",
        "invoiceDate": "2025-01-01",
        "reference": "INV-2025-001",
        "source": "readme-demo"
      }
    }
  }'
```
Expected behavior:
- Webhook returns something like
  `{"status":"ok","handledBy":"create_invoice_entry"}`
- n8n routes into the Create Invoice workflow
- The HTTP Request node receives the JSON from `analysis.fields` (mock
  invoice API)

[Invoice_entry created from n8n](docs/screenshots/create_invoice_entry.png)
2) `create_support_ticket` (ClickUp ticket flow)
```
curl -X POST "http://localhost:5678/webhook/ai-action" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "demo-doc-2",
    "status": "COMPLETED",
    "actionType": "create_support_ticket",
    "type": "RAG_ANSWER",
    "analysis": {
      "type": "RAG_ANSWER",
      "actionType": "create_support_ticket",
      "fields": {
        "title": "Login issue: user cannot sign in",
        "query": "Customer reports they cannot log in to their account.",
        "answer": "Ticket created for login issue; ask customer to provide username and timestamp.",
        "source": "readme-demo"
      }
    }
  }'
```
Expected behavior:
- Webhook returns something like `{"status":"ok","handledBy":"create_support_ticket"}`
- n8n routes into the Create Support Ticket workflow
- ClickUp node creates a task:
  - Name = analysis.fields.query
  - Description = analysis.fields.answer

[ClickUp ticket created from n8n](docs/screenshots/create_support_ticket.png)

3) Unknown/unsupported action (fallback flow)
```
curl -X POST "http://localhost:5678/webhook/ai-action" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "demo-doc-3",
    "status": "COMPLETED",
    "actionType": "do_something_weird",
    "type": "RAG_ANSWER",
    "analysis": {
      "type": "RAG_ANSWER",
      "actionType": "do_something_weird",
      "fields": {
        "note": "This is an unsupported actionType used to test the fallback path.",
        "source": "readme-demo"
      }
    }
  }'
```
Expected behavior:
- Webhook returns `{"status":"ok","handledBy":"unknown"}`
- n8n routes into the Unknown ActionType fallback path
- No business action is executed; the event is only logged/observed

These three calls prove that:
- The webhook is always available in “prod mode” (/webhook/ai-action, no test mode)
- Action routing is fully data-driven via actionType
- Fallback handling for unknown actions works without breaking the system

[Error:unknown created from n8n](docs/screenshots/error:unknown.png)

## Automation & n8n Workflows

The Automation Gateway integrates with **n8n** as an automation engine.
AI analysis results are mapped to explicit `actionType` values, which are
used to route events into dedicated n8n workflows via a webhook.

### ActionType-based Routing

Each processed document may result in one of the following outcomes:

| actionType               | Behavior |
|--------------------------|----------|
| `create_invoice_entry`   | Triggers an automation to create an invoice entry |
| `create_support_ticket`  | Creates a support task (e.g. ClickUp / ticket system) |
| _unknown_                | Logged safely without triggering automation |

- The **success path** of the Java service emits `create_invoice_entry`.
- The **error path** (e.g. RAG failure) emits `create_support_ticket` so that
  failures still surface as a human-visible ticket instead of being dropped.
- The `unknown` branch exists in the n8n Switch node as a future-proof
  extension point. The Java demo does not currently emit other `actionType`s.

### Fallback Handling (Unknown Actions)

The n8n workflow keeps an **“unknown”** output on the Switch node.
If a future version of the backend were to emit a new `actionType` that is
not explicitly configured, this branch can:

- Log the event,
- Store it for later review,
- Or notify an operator.

This design follows a **safety-first automation approach** and prevents
over-automation while keeping all AI decisions observable and auditable.
---

### Workflow Definitions

Exported n8n workflows are stored in the repository under:
`n8n/workflows`

These JSON files can be imported directly into n8n and represent the
operational automation layer used by the Automation Gateway, including:

- The router workflow that:
  - Receives `actionType` + analysis data from the Webhook
  - Routes to the invoice, support ticket, or unknown branch
- The ClickUp ticket branch used for `create_support_ticket`
- The mock invoice branch that POSTs to `https://httpbin.org/post`

---
### Fallback Workflow: unknown_action_type

Not every AI analysis should result in an automation.
If the system derives an `actionType` that is not explicitly supported,
the event is routed to a dedicated fallback workflow:

`n8n/workflows/unknown_action_type.json`:

This workflow does **not** execute any business action.
Instead, it logs the event (or stores it for review), ensuring that:

- Unsupported intents do not trigger unintended automations
- All AI decisions remain observable
- Process mappings and prompts can be iteratively improved

This design follows a **safety-first automation approach** and prevents
over-automation while preserving transparency.

---
## Architecture
- **Java 21**, **Spring Boot 3**
- **Python RAG Agent** reachable via `RagDirectService`
- **H2 Database** for local development
- **Docker Compose** setup for combined deployment

           ┌──────────────────────────────┐
           │   Client / Frontend / API    │
           └───────────────┬──────────────┘
                           │
                           ▼
           ┌──────────────────────────────┐
           │     Spring Boot Gateway      │
           │  (Automation Gateway)        │
           │                              │
           │ • Receives documents         │
           │ • Persists to H2 database    │
           │ • Calls Python RAG service   │
           └───────────────┬──────────────┘
                           │ HTTP
                           ▼
           ┌──────────────────────────────┐
           │     Python RAG-Agent         │
           │ • Embeddings + vector search │
           │ • Context retrieval (Qdrant) │
           │ • LLM-based reasoning        │
           └───────────────┬──────────────┘
                           │
                           ▼
           ┌──────────────────────────────┐
           │            Qdrant            │
           │ • Vector similarity search   │
           └──────────────────────────────┘

n8n runs alongside this stack and exposes an internal webhook. The Java
gateway calls this webhook with:
- `actionType`
- Document metadata (`documentId`)
- AI analysis payload (`analysis.fields.*)

The webhook workflow then fans out to the individual automation branches.

---
## REST API Endpoints
### POST `/api/documents`
Submit a document for AI-based processing.
### GET `/api/documents/{id}`
Retrieve the processed document including AI analysis.

OpenAPI/Swagger UI available at:
```
http://localhost:8080/swagger-ui/index.html
```
---
## Technical foundation of the system
Automation Gateway is a Java 21 / Spring Boot 3 backend designed to 
integrate seamlessly with a standalone Python RAG Agent Service.
It receives documents, stores them, forwards them through an AI pipeline, 
and returns rich, structured analysis results.

n8n runs as an internal automation engine and does not need to be accessed directly in production;
workflow execution is fully observable via automation-gateway logs.


This project demonstrates:
- Microservice architecture (Java ↔ Python)
- Vector search with Qdrant
- AI-powered content enrichment
- Clean Spring engineering: JPA, DTOs, service layers, REST controllers
- Full Docker Compose integration (one command to run the entire stack)
---
## Features
- REST API (`/api/documents`)
- Document persistence (H2 in-memory)
- Integration with Python RAG Agent Service
- n8n-based automation for mock invoice and support ticket (ClickUp) creation 
- Docker-ready architecture
- JUnit test suite (services, controllers, DTOs, repository)

---
## Project Structure
```
src/main/java/com/example/automationgateway
  config/
  controller/
  dto/
  model/
  repository/
  service/
n8n/
  workflows/
```
---
## Technology Stack
- Java 21, Spring Boot 3
- H2 Database (in-memory)
- Vector DB: Qdrant
- Python RAG Agent (microservice)
- n8n for automation routing
- Docker Compose for multi-service orchestration
- JUnit 5 for testing
---
# MIT License

Copyright (c) 2025 Punschkrapferl

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```