# Automation Gateway
## Overview
Automation Gateway is a Spring Boot–based backend that processes documents,
delegates analysis to a Python RAG service, and returns structured AI analysis results.
## Features
- REST API (`/api/documents`)
- Document persistence (H2 in-memory)
- Integration with Python RAG Agent Service
- Docker-ready architecture
- Full JUnit test suite (services, controllers, DTOs, repository)
## Architecture
- **Java 21**, **Spring Boot 3**
- **Python RAG Agent** reachable via `RagDirectService`
- **H2 Database** for local development
- **Docker Compose** setup for combined deployment
## Endpoints
### POST `/api/documents`
Submit a document for AI-based processing.
### GET `/api/documents/{id}`
Retrieve the processed document including AI analysis.
## Running Locally
```
mvn spring-boot:run
```
RAG service must run on:
```
http://localhost:8000/api/query
```
## Docker (Recommended)
```
docker compose up --build
```
## Testing
```
mvn test
```
Includes:
- Unit tests
- Controller tests
- Repository tests
- DTO validation tests
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