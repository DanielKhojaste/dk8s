# DK8s

An event-driven engineering operations console for registering, monitoring, and managing the lifecycle of containerized services.

Engineers use DK8s to see which services are healthy, which version is deployed, what was deployed recently, and what operational events occurred, and to request deployments, restarts, and configuration changes.

> **Status:** early development, working toward `v0.1.0`. See [CHANGELOG.md](CHANGELOG.md).

## Planned stack

- **Backend:** Java 17, Spring Boot, Spring Data JPA, PostgreSQL, Flyway
- **Frontend:** React, TypeScript, Vite
- **Messaging:** Solace PubSub+
- **Infrastructure:** Docker, Docker Compose
