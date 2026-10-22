# DK8s

An event-driven engineering operations console for registering, monitoring, and managing the lifecycle of containerized services.

Engineers use DK8s to see which services are healthy, which version is deployed, what was deployed recently, and what operational events occurred, and to request deployments, restarts, and configuration changes.

> **Status:** `v0.1.0`. DK8s has a service registry and deployment history (REST API + PostgreSQL) and a React console. Deployments are **simulated**: they're recorded and complete immediately; nothing is actually deployed yet. See [CHANGELOG.md](CHANGELOG.md).

## What works today

- **`control-api`**: a Java 17 / Spring Boot 4.1 REST API for registering managed services and deploying new versions (simulated), with deployment history
- **PostgreSQL 17** for persistence, started locally with Docker Compose
- **Flyway** migrations that create and version the database schema
- **Integration tests** that run against a real PostgreSQL container (Testcontainers)
- **`frontend`**: a React + TypeScript console (Vite, React Router, TanStack Query) with a dashboard, service list, service details (deploy form + deployment history), and a register-service form

## Prerequisites

- Java 17 or newer
- Node.js 22.22 or newer (for the frontend)
- Docker Desktop (running)

Maven doesn't need to be installed, because `control-api` includes the Maven Wrapper (`./mvnw`).

## Running locally

Commands are for Git Bash, run from the repository root unless noted.

**1. Create your local `.env`** (first time only)

```bash
cp .env.example .env
```

`.env` holds your local settings and is ignored by git. `.env.example` is the committed template with safe, disposable values. The defaults work as-is. If you change a password or port in `.env`, change the matching `DB_*` value too (see [Configuration](#configuration)).

**2. Start PostgreSQL**

```bash
docker compose up -d postgres
```

Docker Compose reads `.env` automatically and uses it to fill in `compose.yaml`. `-d` runs the container in the background.

**3. Check that it's healthy**

```bash
docker compose ps
```

Wait until the `STATUS` column says `(healthy)`.

**4. Load `.env` into your terminal**

```bash
set -a
source .env
set +a
```

Spring Boot doesn't read `.env` files. These three lines export every variable in `.env` into the current Git Bash session, so programs started from it (like Java) can see `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Run them again in each new terminal, and after editing `.env`.

**5. Run the control API**

```bash
cd control-api
./mvnw spring-boot:run
```

Flyway applies any pending migrations on startup. The API listens on `http://localhost:8080`.

**6. Check it's up**

```bash
curl localhost:8080/actuator/health
```

### Configuration

All local settings live in `.env` (copied from `.env.example`). Two different programs read it:

| Variable | Read by | Purpose | Default |
|---|---|---|---|
| `POSTGRES_DB` | Docker Compose → postgres container | Database to create | `dk8s` |
| `POSTGRES_USER` | Docker Compose → postgres container | User to create | `dk8s` |
| `POSTGRES_PASSWORD` | Docker Compose → postgres container | That user's password | `dk8s` |
| `POSTGRES_PORT` | Docker Compose | Port on your machine that forwards to PostgreSQL | `5432` |
| `DB_URL` | `control-api` (Spring Boot) | JDBC URL to connect to | `jdbc:postgresql://localhost:5432/dk8s` |
| `DB_USERNAME` | `control-api` (Spring Boot) | User to log in as | `dk8s` |
| `DB_PASSWORD` | `control-api` (Spring Boot) | Password to log in with | `dk8s` |

The `DB_*` values must match the `POSTGRES_*` values, and the port in `DB_URL` must equal `POSTGRES_PORT`. If a variable isn't set, both `compose.yaml` and `application.yaml` fall back to the defaults above.

**Changing the database password after the first start:** PostgreSQL only uses `POSTGRES_*` the first time it creates its data volume. To apply new credentials, delete the volume (this erases local data) and start again:

```bash
docker compose down -v
docker compose up -d postgres
```

**7. Run the frontend** (in a second terminal)

```bash
cd frontend
npm install        # first time only
npm run dev
```

Open `http://localhost:5173`. The Vite dev server forwards `/api` requests to `control-api` on port 8080, so the backend must be running. See [frontend/README.md](frontend/README.md) for details.

## REST API

| Method | Path | Description | Responses |
|---|---|---|---|
| `GET` | `/api/services` | List managed services, sorted by name | `200` |
| `GET` | `/api/services/{id}` | Get one managed service | `200`, `404` |
| `POST` | `/api/services` | Register a managed service | `201`, `400`, `409` |
| `GET` | `/api/services/{id}/deployments` | Deployment history, newest first | `200`, `404` |
| `GET` | `/api/services/{id}/deployments/{deploymentId}` | Get one deployment | `200`, `404` |
| `POST` | `/api/services/{id}/deployments` | Deploy a new version (simulated) | `201`, `400`, `404`, `409` |
| `GET` | `/actuator/health` | Application health | `200` |

### Register a service

```bash
curl -i -X POST localhost:8080/api/services \
  -H "Content-Type: application/json" \
  -d '{"name": "payments-api", "description": "Handles card payments", "version": "1.4.2"}'
```

```json
{
  "id": 1,
  "name": "payments-api",
  "description": "Handles card payments",
  "version": "1.4.2",
  "status": "UNKNOWN",
  "createdAt": "2026-10-20T23:47:28.224502Z",
  "updatedAt": "2026-10-20T23:47:28.224502Z"
}
```

Request rules:

- `name`: required, unique, at most 63 characters, made of lowercase letters, digits, and hyphens (for example `payments-api`)
- `version`: required, at most 50 characters
- `description`: optional, at most 500 characters

New services start with status `UNKNOWN`. The possible statuses are `HEALTHY`, `DEGRADED`, `FAILED`, and `UNKNOWN`.

### Deploy a new version

```bash
curl -i -X POST localhost:8080/api/services/1/deployments \
  -H "Content-Type: application/json" \
  -d '{"version": "1.5.0"}'
```

```json
{
  "id": 1,
  "serviceId": 1,
  "version": "1.5.0",
  "previousVersion": "1.4.2",
  "status": "SUCCEEDED",
  "failureReason": null,
  "requestedAt": "2026-10-22T21:06:10.512304Z",
  "completedAt": "2026-10-22T21:06:10.512871Z"
}
```

In this release, deployments are simulated: they complete immediately, update the service's `version`, and set its status to `HEALTHY`. Deployment statuses are `PENDING`, `IN_PROGRESS`, `SUCCEEDED`, and `FAILED`. Requesting a deployment while another one for the same service is still `PENDING` or `IN_PROGRESS` returns `409`.

### Errors

Errors use the standard [problem details](https://www.rfc-editor.org/rfc/rfc9457) JSON format. A validation failure looks like this:

```json
{
  "status": 400,
  "title": "Validation failed",
  "detail": "One or more fields are invalid",
  "instance": "/api/services",
  "errors": { "version": "must not be blank" }
}
```

## Running tests

Docker must be running for the backend tests, because they start their own throwaway PostgreSQL container.

```bash
cd control-api
./mvnw test
```

Frontend tests don't need the backend or Docker:

```bash
cd frontend
npm test
```

## Project structure

```text
compose.yaml                      Local PostgreSQL
frontend/                         React + TypeScript console (see frontend/README.md)
control-api/
  src/main/java/com/dk8s/controlapi/
    controller/                   REST endpoints
    service/                      Business logic
    repository/                   Database access (Spring Data JPA)
    entity/                       JPA entities and enums
    dto/                          API request/response shapes
    exception/                    Error types and JSON error handling
  src/main/resources/
    application.yaml              App configuration
    db/migration/                 Flyway SQL migrations
  src/test/java/...               Integration tests
```
