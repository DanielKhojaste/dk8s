package com.dk8s.controlapi.controller;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.dk8s.controlapi.dto.MessagingStatusResponse;
import com.dk8s.controlapi.messaging.BrokerMonitor;

@RestController
public class MessagingController {

	private static final Logger log = LoggerFactory.getLogger(MessagingController.class);

	private final BrokerMonitor brokerMonitor;

	public MessagingController(BrokerMonitor brokerMonitor) {
		this.brokerMonitor = brokerMonitor;
	}

	/**
	 * Consumers and waiting messages for each DK8s queue. Always 200: if the broker can't be
	 * reached, that's reported in the body (it's the answer to "how is messaging doing?").
	 */
	@GetMapping("/api/messaging/queues")
	public MessagingStatusResponse getQueues() {
		try {
			return new MessagingStatusResponse(true, brokerMonitor.dk8sQueues(), Instant.now());
		}
		catch (RestClientException ex) {
			log.warn("Could not read queue status from the broker: {}", ex.getMessage());
			return new MessagingStatusResponse(false, List.of(), Instant.now());
		}
	}

}
