# DK8s

An event-driven engineering operations console for registering, monitoring, and managing the lifecycle of containerized services.

Engineers use DK8s to see which services are healthy, which version is deployed, what was deployed recently, and what operational events occurred, and to request deployments, restarts, and configuration changes.

> **Status:** early development, working toward `v0.1.0`. Today DK8s has a service registry REST API backed by PostgreSQL. See [CHANGELOG.md](CHANGELOG.md).

## What works today

- **`control-api`**: a Java 17 / Spring Boot 4.1 REST API for registering and viewing managed services
- **PostgreSQL 17** for persistence, started locally with Docker Compose
- **Flyway** migrations that create and version the database schema
- **Integration tests** that run against a real PostgreSQL container (Testcontainers)

## Prerequisites

- Java 17 or newer
- Docker Desktop (running)

Maven doesn't need to be installed, because `control-api` includes the Maven Wrapper (`./mvnw`).

## Running locally

Commands are for Git Bash, run from the repository root.

**1. Start PostgreSQL**

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with a `dk8s` database. The credentials are disposable local-development defaults (`dk8s` / `dk8s`). Data persists in a Docker volume until you run `docker compose down -v`.

**2. Run the control API**

```bash
cd control-api
./mvnw spring-boot:run
```

Flyway applies any pending migrations on startup. The API listens on `http://localhost:8080`.

**3. Check it's up**

```bash
curl localhost:8080/actuator/health
```

### Configuration

The defaults match `compose.yaml`, so no configuration is needed locally. To point at a different database, set:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/dk8s` |
| `DB_USERNAME` | `dk8s` |
| `DB_PASSWORD` | `dk8s` |

## REST API

| Method | Path | Description | Responses |
|---|---|---|---|
| `GET` | `/api/services` | List managed services, sorted by name | `200` |
| `GET` | `/api/services/{id}` | Get one managed service | `200`, `404` |
| `POST` | `/api/services` | Register a managed service | `201`, `400`, `409` |
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

Docker must be running, because the tests start their own throwaway PostgreSQL container.

```bash
cd control-api
./mvnw test
```

## Project structure

```text
compose.yaml                      Local PostgreSQL
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
