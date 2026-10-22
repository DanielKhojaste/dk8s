package com.dk8s.controlapi.messaging;

import java.time.Instant;
import java.util.UUID;

import com.dk8s.controlapi.entity.Deployment;

/**
 * JSON body of every deployment message on Solace. control-api publishes REQUESTED;
 * the worker publishes STARTED, COMPLETED and FAILED. The worker has its own copy of this record.
 */
public record DeploymentMessage(
		String messageId,
		DeploymentMessageType type,
		Long deploymentId,
		Long serviceId,
		String serviceName,
		String version,
		String previousVersion,
		String failureReason,
		Instant occurredAt) {

	public static DeploymentMessage requested(Deployment deployment) {
		return new DeploymentMessage(
				UUID.randomUUID().toString(),
				DeploymentMessageType.REQUESTED,
				deployment.getId(),
				deployment.getService().getId(),
				deployment.getService().getName(),
				deployment.getVersion(),
				deployment.getPreviousVersion(),
				null,
				Instant.now());
	}

}
