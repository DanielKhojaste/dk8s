package com.dk8s.worker.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * JSON body of every deployment message on Solace. control-api publishes REQUESTED;
 * this worker publishes STARTED, COMPLETED and FAILED.
 *
 * control-api has its own copy of this record. The two apps share the JSON <em>format</em>
 * (the contract), not Java code, so each can be built and deployed on its own.
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

	/** A reply about the same deployment, e.g. request.reply(STARTED, null). */
	public DeploymentMessage reply(DeploymentMessageType replyType, String reason) {
		return new DeploymentMessage(UUID.randomUUID().toString(), replyType, deploymentId, serviceId,
				serviceName, version, previousVersion, reason, Instant.now());
	}

}
