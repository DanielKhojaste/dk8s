package com.dk8s.controlapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dk8s.controlapi.dto.ServiceEventResponse;
import com.dk8s.controlapi.service.ServiceEventService;

@RestController
public class ServiceEventController {

	private final ServiceEventService eventService;

	public ServiceEventController(ServiceEventService eventService) {
		this.eventService = eventService;
	}

	/** Newest events across all services. ?limit= defaults to 20 (max 100). */
	@GetMapping("/api/events")
	public List<ServiceEventResponse> getRecent(@RequestParam(defaultValue = "20") int limit) {
		return eventService.findRecent(limit);
	}

	@GetMapping("/api/services/{serviceId}/events")
	public List<ServiceEventResponse> getForService(@PathVariable Long serviceId,
			@RequestParam(defaultValue = "50") int limit) {
		return eventService.findByService(serviceId, limit);
	}

}
