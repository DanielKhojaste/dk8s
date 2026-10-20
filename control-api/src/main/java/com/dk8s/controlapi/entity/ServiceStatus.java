package com.dk8s.controlapi.entity;

/**
 * Health state of a managed service.
 * Stored as text in the database, so the values here must match the
 * CHECK constraint in the managed_services migration.
 */
public enum ServiceStatus {
	HEALTHY,
	DEGRADED,
	FAILED,
	UNKNOWN
}
