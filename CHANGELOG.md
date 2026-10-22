# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Project versioning (`VERSION` file) and this changelog.
- `control-api` Spring Boot service (Java 17, Spring Boot 4.1) with an Actuator health endpoint.
- PostgreSQL 17 for local development via Docker Compose (`compose.yaml`).
- Spring Data JPA and Flyway database migrations; initial `managed_services` table (`V1`).
- Integration tests for the managed service API, running against PostgreSQL in Testcontainers.
- Managed service registry API: `GET /api/services`, `GET /api/services/{id}`, and `POST /api/services` with request validation.
- Consistent JSON error responses (RFC 9457 problem details) for 400, 404, and 409.
- `.env.example` and a documented `.env` workflow for local Docker Compose and `control-api` configuration.
- `frontend` React + TypeScript operations console (Vite, React Router, TanStack Query): dashboard, service list, service detail, and register-service form, using a Vite dev proxy to `control-api`.
- Vitest + Testing Library tests for the service list and registration flow.
