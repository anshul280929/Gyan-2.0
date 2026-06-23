# Gyan

Gyan is a document-grounded AI workspace. Users can create separate chats, upload files into each chat, and ask questions that stay scoped to that chat's documents.

## What It Does

- User authentication with JWT access tokens and refresh-token flow
- Up to 5 chat workspaces per user
- Chat-scoped document upload, preview, download, and delete
- Persisted chat history
- Background document processing with status tracking
- AI question answering over uploaded document context
- Search and indexing pipeline backed by Elasticsearch

## Architecture

The project is split into three app services plus supporting infrastructure:

- `gyan-frontend`
  React + Vite frontend
- `backend`
  Spring Boot API, auth, document management, chat management, and orchestration
- `embedding-service`
  Python FastAPI service for embeddings
- `postgres`
  Primary relational database
- `kafka` + `zookeeper`
  Async document processing pipeline
- `elasticsearch`
  Search index for processed documents

## Architecture Explaination
Frontend (React)
        |
        v
Backend (Spring Boot)
        |
        +------------------+
        |                  |
        v                  v
 PostgreSQL         Kafka Queue
                           |
                           v
                  Document Processor
                           |
                           v
                 Embedding Service
                           |
                           v
                    Elasticsearch



## Tech Stack

### Frontend

- React 19
- React Router
- TypeScript
- Vite

### Backend

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Spring Kafka
- Spring Data Elasticsearch
- Apache Tika
- JWT

### Embedding Service

- FastAPI
- Uvicorn
- sentence-transformers

## Repository Structure

```text
gyan/
├── backend/
├── embedding-service/
├── gyan-frontend/
├── docker-compose.yml
├── docker-compose.azure.yml
├── docker-compose.oracle.yml
├── AZURE_DEPLOY.md
├── ORACLE_VM_DEPLOY.md
└── README.md
```

## Environment Variables

The backend expects these values:

```env
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
GROQ_API_KEY=your_groq_api_key
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gyan
SPRING_ELASTICSEARCH_URIS=http://localhost:9200
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
EMBEDDING_SERVICE_URL=http://localhost:8000
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

For VM and container deployments, use the provided examples:

- [.env.azure.example](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/.env.azure.example)
- [.env.oracle.example](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/.env.oracle.example)

## Local Development

### 1. Start Infrastructure

You need PostgreSQL, Kafka, Zookeeper, Elasticsearch, and the embedding service available.

You can use Docker Compose for infra, or run the full stack through the root compose file if that matches your workflow.

### 2. Run the Backend

From [backend](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/backend):

```bash
mvn spring-boot:run
```

### 3. Run the Frontend

From [gyan-frontend](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/gyan-frontend):

```bash
npm install
npm run dev
```

### 4. Run the Embedding Service

From [embedding-service](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/embedding-service):

```bash
pip install -r requirements.txt
uvicorn embedding_service:app --host 0.0.0.0 --port 8000
```

## Docker Deployment

### Full VM Deployment

Use [docker-compose.oracle.yml](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/docker-compose.oracle.yml) to run:

- frontend
- backend
- postgres
- kafka
- zookeeper
- elasticsearch
- embedding-service

Typical command:

```bash
docker compose -f docker-compose.oracle.yml up -d --build
```

### Azure VM Backend Deployment

Use [docker-compose.azure.yml](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/docker-compose.azure.yml) when hosting only the backend-side stack on a VM:

```bash
docker compose -f docker-compose.azure.yml up -d --build
```

Detailed guides:

- [AZURE_DEPLOY.md](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/AZURE_DEPLOY.md)
- [ORACLE_VM_DEPLOY.md](/C:/Users/manjk/OneDrive/Desktop/Discipline/gyan/ORACLE_VM_DEPLOY.md)

## Common VM Commands

### Pull latest code and redeploy

```bash
cd ~/Gyan
git pull origin main
docker compose -f docker-compose.oracle.yml up -d --build
```

### Rebuild only frontend

```bash
docker compose -f docker-compose.oracle.yml up -d --build frontend
```

### Rebuild only backend

```bash
docker compose -f docker-compose.oracle.yml up -d --build backend
```

### Check running containers

```bash
docker compose -f docker-compose.oracle.yml ps
```

### View logs

```bash
docker compose -f docker-compose.oracle.yml logs --tail=100 backend
docker compose -f docker-compose.oracle.yml logs --tail=100 frontend
```

## Database Operations

### View tables

```bash
docker compose -f docker-compose.oracle.yml exec postgres psql -U postgres -d gyan -c "\dt"
```

### Open PostgreSQL shell

```bash
docker compose -f docker-compose.oracle.yml exec postgres psql -U postgres -d gyan
```

### Reset all table data

This removes all rows and resets IDs:

```bash
docker compose -f docker-compose.oracle.yml exec postgres psql -U postgres -d gyan -c "DO \$\$ DECLARE stmt text; BEGIN SELECT 'TRUNCATE TABLE ' || string_agg(format('%I.%I', schemaname, tablename), ', ') || ' RESTART IDENTITY CASCADE' INTO stmt FROM pg_tables WHERE schemaname = 'public'; IF stmt IS NOT NULL THEN EXECUTE stmt; END IF; END \$\$;"
```

### Clear uploaded files

```bash
docker compose -f docker-compose.oracle.yml exec backend sh -c "rm -rf /app/uploads/*"
```

## Testing Checklist

After a fresh reset, a good end-to-end test flow is:

1. Register a new user
2. Log in
3. Create a chat
4. Upload a document
5. Wait until processing becomes `READY`
6. Preview the document
7. Download the document
8. Ask a question in the chat
9. Delete the document
10. Delete the chat

## Known Operational Notes

- Uploads are limited to 10 MB by the backend
- Frontend nginx should allow slightly above that limit so the app can show proper validation errors
- Older uploaded documents may rely on `filePath` fallback for preview/download
- The app currently uses `spring.jpa.hibernate.ddl-auto=update`
- For production, HTTPS and stronger cookie settings are recommended

## Build Commands

### Frontend

```bash
cd gyan-frontend
npm run build
```

### Backend

```bash
cd backend
mvn compile
```

## API Overview

Key routes include:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /chats`
- `POST /chats`
- `PATCH /chats/{chatId}`
- `DELETE /chats/{chatId}`
- `GET /chats/{chatId}/messages`
- `GET /documents/chats/{chatId}`
- `POST /documents/chats/{chatId}/upload`
- `GET /documents/chats/{chatId}/{id}/preview`
- `GET /documents/chats/{chatId}/{id}/download`
- `DELETE /documents/chats/{chatId}/{id}`
- `POST /ai/chats/{chatId}/ask`

## Security Notes

- Do not commit `.env` or `.env.properties` files
- Rotate any leaked credentials immediately
- Prefer environment variables for secrets
- For production, serve the app behind HTTPS

## Status

This project is actively evolving. The current implementation supports the full document-chat workflow, Docker deployment, and VM-based hosting, with ongoing improvements around deployment polish and operational robustness.
