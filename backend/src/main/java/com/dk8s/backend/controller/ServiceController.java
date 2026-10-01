package com.dk8s.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

	@GetMapping
	public String getServices() {
		return "DK8s services";
	}
}
