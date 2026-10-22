package com.dk8s.controlapi.entity;

/** Must match the CHECK constraint in the service_events migration. */
public enum ServiceEventType {
	SERVICE_REGISTERED,
	DEPLOYMENT_REQUESTED,
	DEPLOYMENT_STARTED,
	DEPLOYMENT_COMPLETED,
	DEPLOYMENT_FAILED
}
