package com.dk8s.worker.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.dk8s.worker.simulation.DeploymentSimulator;

import tools.jackson.databind.json.JsonMapper;

/** Plain unit test: no Spring, no broker. The publisher is a Mockito mock. */
class DeploymentRequestListenerTests {

	private final MessagePublisher publisher = mock(MessagePublisher.class);
	private final DeploymentRequestListener listener = new DeploymentRequestListener(
			publisher, new DeploymentSimulator(Duration.ZERO), JsonMapper.builder().build());

	@Test
	void publishesStartedThenCompleted() {
		listener.onDeploymentRequested(request("1.5.0"));

		ArgumentCaptor<DeploymentMessage> sent = ArgumentCaptor.forClass(DeploymentMessage.class);
		InOrder order = inOrder(publisher);
		order.verify(publisher).publish(eq("dk8s/deployment/started/payments-api"), sent.capture());
		order.verify(publisher).publish(eq("dk8s/deployment/completed/payments-api"), sent.capture());

		DeploymentMessage completed = sent.getAllValues().get(1);
		assertThat(completed.type()).isEqualTo(DeploymentMessageType.COMPLETED);
		assertThat(completed.deploymentId()).isEqualTo(7L);
		assertThat(completed.version()).isEqualTo("1.5.0");
		assertThat(completed.failureReason()).isNull();
	}

	@Test
	void publishesStartedThenFailedWithReason() {
		listener.onDeploymentRequested(request("1.6.0-fail"));

		ArgumentCaptor<DeploymentMessage> sent = ArgumentCaptor.forClass(DeploymentMessage.class);
		InOrder order = inOrder(publisher);
		order.verify(publisher).publish(eq("dk8s/deployment/started/payments-api"), sent.capture());
		order.verify(publisher).publish(eq("dk8s/deployment/failed/payments-api"), sent.capture());
		assertThat(sent.getAllValues().get(1).failureReason()).contains("did not pass its health checks");
	}

	/** The JSON control-api publishes for a REQUESTED message. */
	private static String request(String version) {
		return """
				{"messageId": "m-1", "type": "REQUESTED", "deploymentId": 7, "serviceId": 1,
				 "serviceName": "payments-api", "version": "%s", "previousVersion": "1.4.2",
				 "failureReason": null, "occurredAt": "2026-10-22T21:00:00Z"}
				""".formatted(version);
	}

}
