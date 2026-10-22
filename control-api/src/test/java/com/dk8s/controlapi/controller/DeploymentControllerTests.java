package com.dk8s.controlapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.dk8s.controlapi.TestcontainersConfiguration;
import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceStatus;
import com.dk8s.controlapi.repository.DeploymentRepository;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Exercises /api/services/{id}/deployments end to end against PostgreSQL (Testcontainers).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class DeploymentControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ManagedServiceRepository serviceRepository;

	@Autowired
	private DeploymentRepository deploymentRepository;

	private ManagedService paymentsApi;

	@BeforeEach
	void setUp() {
		deploymentRepository.deleteAll();
		serviceRepository.deleteAll();
		paymentsApi = serviceRepository.save(
				new ManagedService("payments-api", "Handles card payments", "1.4.2", ServiceStatus.UNKNOWN));
	}

	@Test
	void deployRecordsDeploymentAndUpdatesService() throws Exception {
		mockMvc.perform(post("/api/services/{id}/deployments", paymentsApi.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"version": "1.5.0"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesRegex(".*/api/services/\\d+/deployments/\\d+")))
				.andExpect(jsonPath("$.serviceId").value(paymentsApi.getId()))
				.andExpect(jsonPath("$.version").value("1.5.0"))
				.andExpect(jsonPath("$.previousVersion").value("1.4.2"))
				.andExpect(jsonPath("$.status").value("SUCCEEDED"))
				.andExpect(jsonPath("$.completedAt").isNotEmpty());

		mockMvc.perform(get("/api/services/{id}", paymentsApi.getId()))
				.andExpect(jsonPath("$.version").value("1.5.0"))
				.andExpect(jsonPath("$.status").value("HEALTHY"));
	}

	@Test
	void historyIsNewestFirst() throws Exception {
		deploy("1.5.0");
		deploy("1.6.0");

		mockMvc.perform(get("/api/services/{id}/deployments", paymentsApi.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].version").value("1.6.0"))
				.andExpect(jsonPath("$[0].previousVersion").value("1.5.0"))
				.andExpect(jsonPath("$[1].version").value("1.5.0"));
	}

	@Test
	void deployReturns400WhenVersionIsBlank() throws Exception {
		mockMvc.perform(post("/api/services/{id}/deployments", paymentsApi.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"version": ""}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.version").value("must not be blank"));

		assertThat(deploymentRepository.count()).isZero();
	}

	@Test
	void deployReturns404WhenServiceDoesNotExist() throws Exception {
		mockMvc.perform(post("/api/services/{id}/deployments", 999_999)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"version": "1.5.0"}
								"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deployReturns409WhileAnotherDeploymentIsActive() throws Exception {
		deploymentRepository.save(new Deployment(paymentsApi, "1.5.0")); // stays PENDING

		mockMvc.perform(post("/api/services/{id}/deployments", paymentsApi.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"version": "1.6.0"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Deployment already in progress"));
	}

	@Test
	void getByIdReturns404ForDeploymentOfAnotherService() throws Exception {
		ManagedService other = serviceRepository.save(
				new ManagedService("email-worker", null, "3.0.1", ServiceStatus.UNKNOWN));
		Deployment otherDeployment = deploymentRepository.save(new Deployment(other, "3.1.0"));

		mockMvc.perform(get("/api/services/{id}/deployments/{deploymentId}",
						paymentsApi.getId(), otherDeployment.getId()))
				.andExpect(status().isNotFound());
	}

	private void deploy(String version) throws Exception {
		mockMvc.perform(post("/api/services/{id}/deployments", paymentsApi.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"version\": \"" + version + "\"}"))
				.andExpect(status().isCreated());
	}

}
