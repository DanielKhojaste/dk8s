package com.dk8s.controlapi.entity;

/**
 * Lifecycle of a deployment. Must match the CHECK constraint in the deployments migration.
 */
public enum DeploymentStatus {
	PENDING,
	IN_PROGRESS,
	SUCCEEDED,
	FAILED;

	/** True while the deployment hasn't finished yet. */
	public boolean isActive() {
		return this == PENDING || this == IN_PROGRESS;
	}
}
