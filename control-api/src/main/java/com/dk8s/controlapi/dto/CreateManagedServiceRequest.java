package com.dk8s.controlapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/services.
 * Status is not accepted here: a newly registered service always starts as UNKNOWN.
 */
public record CreateManagedServiceRequest(

		@NotBlank
		@Size(max = 63)
		@Pattern(regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?$",
				message = "must be lowercase letters, digits, and hyphens, and start and end with a letter or digit")
		String name,

		@Size(max = 500)
		String description,

		@NotBlank
		@Size(max = 50)
		String version) {
}
