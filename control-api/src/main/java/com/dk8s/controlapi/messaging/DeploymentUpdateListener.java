package com.dk8s.controlapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.dk8s.controlapi.service.DeploymentService;

import tools.jackson.databind.json.JsonMapper;

/**
 * Receives the worker's STARTED / COMPLETED / FAILED messages from the
 * dk8s.control-api.deployment-updates queue and applies them.
 *
 * If this method throws, the message is not acknowledged and Solace redelivers it
 * (up to the queue's max redelivery count).
 */
@Component
public class DeploymentUpdateListener {

	private static final Logger log = LoggerFactory.getLogger(DeploymentUpdateListener.class);

	private final DeploymentService deploymentService;
	private final JsonMapper jsonMapper;

	public DeploymentUpdateListener(DeploymentService deploymentService, JsonMapper jsonMapper) {
		this.deploymentService = deploymentService;
		this.jsonMapper = jsonMapper;
	}

	@JmsListener(destination = "${dk8s.messaging.deployment-updates-queue}")
	public void onDeploymentUpdate(String json) {
		log.info("Received deployment update: {}", json);
		DeploymentMessage update = jsonMapper.readValue(json, DeploymentMessage.class);
		deploymentService.applyUpdate(update);
	}

}
