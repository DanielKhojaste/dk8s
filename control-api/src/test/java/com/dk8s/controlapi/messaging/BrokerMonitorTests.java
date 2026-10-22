package com.dk8s.controlapi.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Unit test: a fake SEMP server answers with the JSON shape the real broker returns.
 */
class BrokerMonitorTests {

	@Test
	void readsConsumersAndWaitingMessagesForDk8sQueuesOnly() {
		RestClient.Builder builder = RestClient.builder()
				.baseUrl("http://broker:8080")
				.defaultHeaders(headers -> headers.setBasicAuth("admin", "admin"));
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		BrokerMonitor monitor = new BrokerMonitor(builder.build(), "default");

		server.expect(requestTo("http://broker:8080/SEMP/v2/monitor/msgVpns/default/queues?count=100"))
				.andExpect(header("Authorization", "Basic YWRtaW46YWRtaW4="))
				.andRespond(withSuccess("""
						{
						  "data": [
						    {"queueName": "dk8s.worker.deployment-requests", "msgVpnName": "default"},
						    {"queueName": "some.other.queue", "msgVpnName": "default"},
						    {"queueName": "dk8s.control-api.deployment-updates", "msgVpnName": "default"}
						  ],
						  "collections": [
						    {"msgs": {"count": 2}, "txFlows": {"count": 0}},
						    {"msgs": {"count": 9}, "txFlows": {"count": 1}},
						    {"msgs": {"count": 0}, "txFlows": {"count": 1}}
						  ],
						  "meta": {"responseCode": 200}
						}
						""", MediaType.APPLICATION_JSON));

		assertThat(monitor.dk8sQueues()).containsExactly(
				new QueueStatus("dk8s.control-api.deployment-updates", 1, 0),
				new QueueStatus("dk8s.worker.deployment-requests", 0, 2));
		server.verify();
	}

}
