package com.dk8s.controlapi.exception;

public class DeploymentNotFoundException extends RuntimeException {

	public DeploymentNotFoundException(Long serviceId, Long deploymentId) {
		super("Deployment " + deploymentId + " was not found for managed service " + serviceId);
	}

}
