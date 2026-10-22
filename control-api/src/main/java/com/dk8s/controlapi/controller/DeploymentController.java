package com.dk8s.controlapi.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dk8s.controlapi.dto.CreateDeploymentRequest;
import com.dk8s.controlapi.dto.DeploymentResponse;
import com.dk8s.controlapi.service.DeploymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services/{serviceId}/deployments")
public class DeploymentController {

	private final DeploymentService deploymentService;

	public DeploymentController(DeploymentService deploymentService) {
		this.deploymentService = deploymentService;
	}

	@GetMapping
	public List<DeploymentResponse> getAll(@PathVariable Long serviceId) {
		return deploymentService.findByService(serviceId);
	}

	@GetMapping("/{deploymentId}")
	public DeploymentResponse getById(@PathVariable Long serviceId, @PathVariable Long deploymentId) {
		return deploymentService.findById(serviceId, deploymentId);
	}

	@PostMapping
	public ResponseEntity<DeploymentResponse> create(@PathVariable Long serviceId,
			@Valid @RequestBody CreateDeploymentRequest request) {
		DeploymentResponse created = deploymentService.requestDeployment(serviceId, request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		// 202 Accepted: the request is saved and queued, but the worker hasn't finished it yet.
		return ResponseEntity.accepted().location(location).body(created);
	}

}
