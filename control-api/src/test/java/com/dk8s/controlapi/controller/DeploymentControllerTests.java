package com.dk8s.controlapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dk8s.controlapi.TestcontainersConfiguration;
import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceStatus;
import com.dk8s.controlapi.exception.MessageBrokerUnavailableException;
import com.dk8s.controlapi.messaging.DeploymentMessage;
import com.dk8s.controlapi.messaging.DeploymentMessageType;
import com.dk8s.controlapi.messaging.DeploymentUpdateListener;
import com.dk8s.controlapi.messaging.MessagePublisher;
import com.dk8s.controlapi.repository.DeploymentRepository;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * The asynchronous deployment lifecycle, end to end against PostgreSQL (Testcontainers).
 * Solace is replaced by a mock publisher (outgoing messages) and by calling the listener
 * directly with the worker's JSON (incoming messages).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class DeploymentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ManagedServiceRepository serviceRepository;

	@Autowired
	private DeploymentRepository deploymentRepository;

	@Autowired
	private DeploymentUpdateListener updateListener;

	@Autowired
	private JdbcTemplate jdbc;

	@MockitoBean
	private MessagePublisher publisher;

	private ManagedService paymentsApi;

	@BeforeEach
	void setUp() {
		jdbc.execute("TRUNCATE service_events, deployments, managed_services RESTART IDENTITY CASCADE");
		paymentsApi = serviceRepository.save(
				new ManagedService("payments-api", "Handles card payments", "1.4.2", ServiceStatus.HEALTHY));
	}

	@Test
	void deployReturns202PendingAndPublishesRequestedMessage() throws Exception {
		mockMvc.perform(deployRequest("1.5.0"))
				.andExpect(status().isAccepted())
				.andExpect(header().string("Location", matchesRegex(".*/api/services/\\d+/deployments/\\d+")))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.version").value("1.5.0"))
				.andExpect(jsonPath("$.previousVersion").value("1.4.2"))
				.andExpect(jsonPath("$.completedAt").isEmpty());

		ArgumentCaptor<DeploymentMessage> sent = ArgumentCaptor.forClass(DeploymentMessage.class);
		verify(publisher).publish(eq("dk8s/deployment/requested/payments-api"), sent.capture());
		assertThat(sent.getValue().type()).isEqualTo(DeploymentMessageType.REQUESTED);
		assertThat(sent.getValue().serviceName()).isEqualTo("payments-api");
		assertThat(sent.getValue().version()).isEqualTo("1.5.0");

		// Nothing has changed on the service yet: the worker hasn't run.
		mockMvc.perform(get("/api/services/{id}", paymentsApi.getId()))
				.andExpect(jsonPath("$.version").value("1.4.2"));
	}

	@Test
	void workerMessagesMoveDeploymentToSucceeded() throws Exception {
		Long deploymentId = requestDeployment("1.5.0");

		updateListener.onDeploymentUpdate(workerMessage("STARTED", deploymentId, null));
		mockMvc.perform(get("/api/services/{id}/deployments/{d}", paymentsApi.getId(), deploymentId))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		updateListener.onDeploymentUpdate(workerMessage("COMPLETED", deploymentId, null));
		mockMvc.perform(get("/api/services/{id}/deployments/{d}", paymentsApi.getId(), deploymentId))
				.andExpect(jsonPath("$.status").value("SUCCEEDED"))
				.andExpect(jsonPath("$.completedAt").isNotEmpty());
		mockMvc.perform(get("/api/services/{id}", paymentsApi.getId()))
				.andExpect(jsonPath("$.version").value("1.5.0"))
				.andExpect(jsonPath("$.status").value("HEALTHY"));
	}

	@Test
	void failedMessageKeepsOldVersionAndDegradesService() throws Exception {
		Long deploymentId = requestDeployment("1.6.0-fail");

		updateListener.onDeploymentUpdate(workerMessage("STARTED", deploymentId, null));
		updateListener.onDeploymentUpdate(workerMessage("FAILED", deploymentId, "Health checks failed"));

		mockMvc.perform(get("/api/services/{id}/deployments/{d}", paymentsApi.getId(), deploymentId))
				.andExpect(jsonPath("$.status").value("FAILED"))
				.andExpect(jsonPath("$.failureReason").value("Health checks failed"));
		mockMvc.perform(get("/api/services/{id}", paymentsApi.getId()))
				.andExpect(jsonPath("$.version").value("1.4.2"))
				.andExpect(jsonPath("$.status").value("DEGRADED"));
	}

	@Test
	void duplicateMessageAfterCompletionIsIgnored() throws Exception {
		Long deploymentId = requestDeployment("1.5.0");
		updateListener.onDeploymentUpdate(workerMessage("COMPLETED", deploymentId, null));

		// Solace may redeliver a message; a late FAILED must not undo the success.
		updateListener.onDeploymentUpdate(workerMessage("FAILED", deploymentId, "duplicate"));

		mockMvc.perform(get("/api/services/{id}/deployments/{d}", paymentsApi.getId(), deploymentId))
				.andExpect(jsonPath("$.status").value("SUCCEEDED"));
	}

	@Test
	void timelineListsEveryStepNewestFirst() throws Exception {
		Long deploymentId = requestDeployment("1.5.0");
		updateListener.onDeploymentUpdate(workerMessage("STARTED", deploymentId, null));
		updateListener.onDeploymentUpdate(workerMessage("COMPLETED", deploymentId, null));

		mockMvc.perform(get("/api/services/{id}/events", paymentsApi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].type").value("DEPLOYMENT_COMPLETED"))
				.andExpect(jsonPath("$[0].topic").value("dk8s/deployment/completed/payments-api"))
				.andExpect(jsonPath("$[1].type").value("DEPLOYMENT_STARTED"))
				.andExpect(jsonPath("$[2].type").value("DEPLOYMENT_REQUESTED"));

		mockMvc.perform(get("/api/events").param("limit", "2"))
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].serviceName").value("payments-api"));
	}

	@Test
	void deployReturns503AndSavesNothingWhenBrokerIsDown() throws Exception {
		doThrow(new MessageBrokerUnavailableException(new RuntimeException("connection refused")))
				.when(publisher).publish(anyString(), any());

		mockMvc.perform(deployRequest("1.5.0"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.title").value("Message broker unavailable"));

		assertThat(deploymentRepository.count()).isZero();
		assertThat(jdbc.queryForObject("SELECT count(*) FROM service_events", Integer.class)).isZero();
	}

	@Test
	void deployReturns400WhenVersionIsBlank() throws Exception {
		mockMvc.perform(deployRequest(""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.version").value("must not be blank"));
	}

	@Test
	void deployReturns404WhenServiceDoesNotExist() throws Exception {
		mockMvc.perform(post("/api/services/{id}/deployments", 999_999)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\": \"1.5.0\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deployReturns409WhileAnotherDeploymentIsActive() throws Exception {
		requestDeployment("1.5.0"); // stays PENDING: no worker in this test

		mockMvc.perform(deployRequest("1.6.0"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Deployment already in progress"));
	}

	@Test
	void getByIdReturns404ForDeploymentOfAnotherService() throws Exception {
		ManagedService other = serviceRepository.save(
				new ManagedService("email-worker", null, "3.0.1", ServiceStatus.UNKNOWN));
		Deployment otherDeployment = deploymentRepository.save(new Deployment(other, "3.1.0"));

		mockMvc.perform(get("/api/services/{id}/deployments/{d}", paymentsApi.getId(), otherDeployment.getId()))
				.andExpect(status().isNotFound());
	}

	private org.springframework.test.web.servlet.RequestBuilder deployRequest(String version) {
		return post("/api/services/{id}/deployments", paymentsApi.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"version\": \"" + version + "\"}");
	}

	private Long requestDeployment(String version) throws Exception {
		mockMvc.perform(deployRequest(version)).andExpect(status().isAccepted());
		return deploymentRepository.findByServiceIdOrderByRequestedAtDescIdDesc(paymentsApi.getId()).get(0).getId();
	}

	/** The JSON the worker would publish (same shape as DeploymentMessage). */
	private String workerMessage(String type, Long deploymentId, String failureReason) {
		String reason = failureReason == null ? "null" : "\"" + failureReason + "\"";
		return """
				{"messageId": "test-%s", "type": "%s", "deploymentId": %d, "serviceId": %d,
				 "serviceName": "payments-api", "version": "x", "previousVersion": "1.4.2",
				 "failureReason": %s, "occurredAt": "2026-10-22T21:00:00Z"}
				""".formatted(type, type, deploymentId, paymentsApi.getId(), reason);
	}

}
