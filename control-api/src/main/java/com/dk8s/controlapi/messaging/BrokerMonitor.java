package com.dk8s.controlapi.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

/**
 * Asks the Solace broker about the DK8s queues using its SEMP v2 <em>monitor</em> API
 * (read-only statistics), e.g. "how many consumers are connected and how many messages are waiting?".
 *
 * <pre>
 * GET /SEMP/v2/monitor/msgVpns/default/queues
 * {
 *   "data":        [ { "queueName": "dk8s.worker.deployment-requests", ... }, ... ],
 *   "collections": [ { "msgs": { "count": 1 }, "txFlows": { "count": 0 } }, ... ]
 * }
 * </pre>
 *
 * data[i] and collections[i] describe the same queue. msgs.count is the number of waiting messages;
 * txFlows.count is the number of consumers ("transmit flows" from the broker to a client).
 */
public class BrokerMonitor {

	private static final String DK8S_QUEUE_PREFIX = "dk8s.";

	private final RestClient sempClient;
	private final String msgVpn;

	public BrokerMonitor(RestClient sempClient, String msgVpn) {
		this.sempClient = sempClient;
		this.msgVpn = msgVpn;
	}

	/** @throws org.springframework.web.client.RestClientException if the broker can't be reached */
	public List<QueueStatus> dk8sQueues() {
		JsonNode response = sempClient.get()
				.uri("/SEMP/v2/monitor/msgVpns/{vpn}/queues?count=100", msgVpn)
				.retrieve()
				.body(JsonNode.class);

		JsonNode data = response.path("data");
		JsonNode collections = response.path("collections");

		List<QueueStatus> queues = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			String name = data.get(i).path("queueName").asString();
			if (!name.startsWith(DK8S_QUEUE_PREFIX)) {
				continue;
			}
			JsonNode counts = collections.path(i);
			queues.add(new QueueStatus(
					name,
					counts.path("txFlows").path("count").asInt(0),
					counts.path("msgs").path("count").asLong(0)));
		}
		queues.sort((a, b) -> a.name().compareTo(b.name()));
		return queues;
	}

}
