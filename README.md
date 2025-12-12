# Automation Gateway

Automation Gateway is a Spring Boot backend that orchestrates document
processing, AI analysis, vector search, and downstream automation workflows.

It demonstrates:
- AI-driven document analysis (RAG)
- Microservice orchestration (Java ↔ Python)
- Event-driven automation (n8n)
- Production-style Docker deployment
- Observable execution via logs (no UI required)

(The Automation Gateway image is versioned (v1.0.0) to ensure reproducible demos.)

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
Verify automation (recommended)
```
docker-compose -f docker-compose.demo.yml logs -f automation-gateway
```
Expected behavior:
- AI analysis is performed
- An action type is derived
- A downstream automation workflow is triggered
- Execution is confirmed via logs
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
- actionType: ```create_support_ticket```
- Support automation workflow triggered via n8n
- Execution visible in logs

### Knowledge Query (RAG only)
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Explain quantum computing in simple terms."
  }'
```
Expected behavior:
- AI analysis
- No automation triggered
- System correctly avoids over-automation

### Unsupported/Unknown Intent
```
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Do something completely unrelated with no clear intent."
  }'
```
Expected behavior:
- No matching actionType
- Routed to fallback path
- Logged as unknown
---
## Technical foundation of the system
Automation Gateway is a Java 21 / Spring Boot 3 backend designed to 
integrate seamlessly with a standalone Python RAG Agent Service.
It receives documents, stores them, forwards them through an AI pipeline, 
and returns rich, structured analysis results.

n8n runs as an internal automation engine and does not need to be accessed directly;
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
- Docker-ready architecture
- Full JUnit test suite (services, controllers, DTOs, repository)
---
# What the System does
## End-to-End Flow
1. Client sends text via ```POST /api/documents```
2. Spring stores the document in H2
3. Spring calls the Python RAG Agent:
```http://rag-api:8000/api/query```
4. RAG Agent:
- Embeds text
- Retrieves similar context from Qdrant
- Runs AI reasoning
- Produces structured Analysis
5. Spring attaches the AI analysis to the document record
6. Response returned to client

## Example output:
``` 
{
  "id": 1,
  "text": "Explain quantum computing simply.",
  "aiAnalysis": {
    "summary": "Quantum computing uses qubits instead of bits…",
    "keywords": ["qubits", "superposition", "entanglement"],
    "confidence": 0.92
  }
}
```
---
## Architecture
- **Java 21**, **Spring Boot 3**
- **Python RAG Agent** reachable via `RagDirectService`
- **H2 Database** for local development
- **Docker Compose** setup for combined deployment

           ┌──────────────────────────────┐
           │   Client / Frontend / API     │
           └───────────────┬──────────────┘
                           │
                           ▼
           ┌──────────────────────────────┐
           │     Spring Boot Gateway       │
           │  (Automation Gateway)         │
           │                                │
           │ • Receives documents          │
           │ • Persists to H2 database      │
           │ • Calls Python RAG service     │
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
           │            Qdrant             │
           │ • Vector similarity search    │
           └──────────────────────────────┘
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
## Project Structure
```
src/main/java/com/example/automationgateway
config/
controller/
dto/
model/
repository/
service/
```
---
## Technology Stack
- Java 21, Spring Boot 3
- H2 Database (in-memory)
- Vector DB: Qdrant
- Python RAG Agent (microservice)
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