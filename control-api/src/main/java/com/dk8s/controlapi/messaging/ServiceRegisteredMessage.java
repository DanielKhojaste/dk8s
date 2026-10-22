package com.dk8s.controlapi.messaging;

import java.time.Instant;
import java.util.UUID;

import com.dk8s.controlapi.entity.ManagedService;

/** JSON body published when a new service is registered. */
public record ServiceRegisteredMessage(
		String messageId,
		Long serviceId,
		String serviceName,
		String version,
		Instant occurredAt) {

	public static ServiceRegisteredMessage from(ManagedService service) {
		return new ServiceRegisteredMessage(
				UUID.randomUUID().toString(),
				service.getId(),
				service.getName(),
				service.getVersion(),
				Instant.now());
	}

}
