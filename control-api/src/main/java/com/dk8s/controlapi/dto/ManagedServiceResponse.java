package com.dk8s.controlapi.dto;

import java.time.Instant;

import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceStatus;

/**
 * JSON representation of a managed service returned by the API.
 */
public record ManagedServiceResponse(
		Long id,
		String name,
		String description,
		String version,
		ServiceStatus status,
		Instant createdAt,
		Instant updatedAt) {

	public static ManagedServiceResponse from(ManagedService service) {
		return new ManagedServiceResponse(
				service.getId(),
				service.getName(),
				service.getDescription(),
				service.getVersion(),
				service.getStatus(),
				service.getCreatedAt(),
				service.getUpdatedAt());
	}

}
