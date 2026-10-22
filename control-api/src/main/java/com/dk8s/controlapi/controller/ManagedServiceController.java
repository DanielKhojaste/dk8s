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

import com.dk8s.controlapi.dto.CreateManagedServiceRequest;
import com.dk8s.controlapi.dto.ManagedServiceResponse;
import com.dk8s.controlapi.service.ManagedServiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ManagedServiceController {

	private final ManagedServiceService managedServiceService;

	public ManagedServiceController(ManagedServiceService managedServiceService) {
		this.managedServiceService = managedServiceService;
	}

	@GetMapping
	public List<ManagedServiceResponse> getAll() {
		return managedServiceService.findAll();
	}

	@GetMapping("/{id}")
	public ManagedServiceResponse getById(@PathVariable Long id) {
		return managedServiceService.findById(id);
	}

	@PostMapping
	public ResponseEntity<ManagedServiceResponse> create(@Valid @RequestBody CreateManagedServiceRequest request) {
		ManagedServiceResponse created = managedServiceService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

}
