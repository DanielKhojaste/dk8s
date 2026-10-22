package com.dk8s.controlapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/services/{id}/deployments.
 */
public record CreateDeploymentRequest(

		@NotBlank
		@Size(max = 50)
		String version) {
}
