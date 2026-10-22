# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Solace PubSub+ Standard broker in Docker Compose, with a one-off `solace-init` job that creates the DK8s queues and topic subscriptions through the SEMP v2 management API.
- `control-api` publishes `deployment.requested` and `service.registered` messages to Solace topics (`dk8s/deployment/requested/{service}`, `dk8s/service/registered/{service}`) and consumes the worker's started/completed/failed messages from the `dk8s.control-api.deployment-updates` queue.
- `worker` Spring Boot service: consumes deployment requests from the `dk8s.worker.deployment-requests` queue, publishes `started`, then `completed` or `failed`. Deployments are simulated (default 3 s); a version containing `fail` (e.g. `1.6.0-fail`) fails on purpose to demonstrate the failure path.
- Operational event timeline (`service_events` table, `V3` migration): `GET /api/events` and `GET /api/services/{id}/events`.
- `503 Message broker unavailable` when Solace can't be reached; the request is rolled back so nothing is half-saved.
- `GET /api/messaging/queues`: consumers and waiting messages for each DK8s queue, read from the broker's SEMP v2 monitor API (`brokerReachable: false` if the broker can't be reached).
- Frontend: live deployment progress (polls while a deployment is pending or in progress), an event timeline on each service page and a "Recent events" feed on the dashboard, each showing the Solace topic, and a clear message when the broker is unavailable.

### Changed

- `POST /api/services/{id}/deployments` now returns `202 Accepted` with a `PENDING` deployment; the worker completes it asynchronously. A failed deployment keeps the previous version and marks the service `DEGRADED`.

## [0.1.0] - 2026-10-22

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
- Deployments: `POST /api/services/{id}/deployments` deploys a new version (simulated; completes immediately), with history via `GET /api/services/{id}/deployments` and `GET /api/services/{id}/deployments/{deploymentId}`. Successful deployments update the service's version and set it to `HEALTHY`; a second deployment while one is active returns 409.
- `deployments` table (`V2` migration) linked to `managed_services`.
- Service detail page: deploy form and deployment history (previous → new version, status, timestamps).

[Unreleased]: https://github.com/DanielKhojaste/dk8s/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/DanielKhojaste/dk8s/releases/tag/v0.1.0
