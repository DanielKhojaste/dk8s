package com.dk8s.controlapi.dto;

import java.time.Instant;

import com.dk8s.controlapi.entity.ServiceEvent;
import com.dk8s.controlapi.entity.ServiceEventType;

/**
 * JSON representation of a timeline event.
 */
public record ServiceEventResponse(
		Long id,
		Long serviceId,
		String serviceName,
		Long deploymentId,
		ServiceEventType type,
		String summary,
		String topic,
		Instant occurredAt) {

	public static ServiceEventResponse from(ServiceEvent event) {
		return new ServiceEventResponse(
				event.getId(),
				event.getService().getId(),
				event.getService().getName(),
				event.getDeploymentId(),
				event.getType(),
				event.getSummary(),
				event.getTopic(),
				event.getOccurredAt());
	}

}
