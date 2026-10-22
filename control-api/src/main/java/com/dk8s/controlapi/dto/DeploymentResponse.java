package com.dk8s.controlapi.dto;

import java.time.Instant;

import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.DeploymentStatus;

/**
 * JSON representation of a deployment returned by the API.
 */
public record DeploymentResponse(
		Long id,
		Long serviceId,
		String version,
		String previousVersion,
		DeploymentStatus status,
		String failureReason,
		Instant requestedAt,
		Instant completedAt) {

	public static DeploymentResponse from(Deployment deployment) {
		return new DeploymentResponse(
				deployment.getId(),
				deployment.getService().getId(),
				deployment.getVersion(),
				deployment.getPreviousVersion(),
				deployment.getStatus(),
				deployment.getFailureReason(),
				deployment.getRequestedAt(),
				deployment.getCompletedAt());
	}

}
