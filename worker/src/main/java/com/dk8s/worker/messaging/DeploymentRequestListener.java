package com.dk8s.worker.messaging;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.dk8s.worker.simulation.DeploymentSimulator;

import tools.jackson.databind.json.JsonMapper;

/**
 * Takes deployment requests from the dk8s.worker.deployment-requests queue, one at a time:
 * publishes STARTED, runs the (simulated) deployment, then publishes COMPLETED or FAILED.
 *
 * The message is acknowledged only when this method returns normally. If the worker crashes
 * halfway, Solace keeps the request and redelivers it when the worker comes back.
 */
@Component
public class DeploymentRequestListener {

	private static final Logger log = LoggerFactory.getLogger(DeploymentRequestListener.class);

	private final MessagePublisher publisher;
	private final DeploymentSimulator simulator;
	private final JsonMapper jsonMapper;

	public DeploymentRequestListener(MessagePublisher publisher, DeploymentSimulator simulator, JsonMapper jsonMapper) {
		this.publisher = publisher;
		this.simulator = simulator;
		this.jsonMapper = jsonMapper;
	}

	@JmsListener(destination = "${dk8s.messaging.deployment-requests-queue}")
	public void onDeploymentRequested(String json) {
		DeploymentMessage request = jsonMapper.readValue(json, DeploymentMessage.class);
		log.info("Deploying {} {} (deployment {})", request.serviceName(), request.version(), request.deploymentId());

		publish(request.reply(DeploymentMessageType.STARTED, null));

		Optional<String> failure = simulator.deploy(request.serviceName(), request.version());

		if (failure.isPresent()) {
			log.warn("Deployment {} failed: {}", request.deploymentId(), failure.get());
			publish(request.reply(DeploymentMessageType.FAILED, failure.get()));
		}
		else {
			log.info("Deployment {} completed", request.deploymentId());
			publish(request.reply(DeploymentMessageType.COMPLETED, null));
		}
	}

	private void publish(DeploymentMessage message) {
		publisher.publish(Topics.deployment(message.type(), message.serviceName()), message);
	}

}
