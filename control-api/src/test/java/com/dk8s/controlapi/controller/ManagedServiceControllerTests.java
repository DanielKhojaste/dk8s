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
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceStatus;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Exercises the /api/services endpoints end to end: HTTP request -> controller ->
 * service -> repository -> real PostgreSQL (Testcontainers) with Flyway migrations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ManagedServiceControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ManagedServiceRepository repository;

	@BeforeEach
	void clearDatabase() {
		repository.deleteAll();
	}

	@Test
	void createReturns201WithLocationAndUnknownStatus() throws Exception {
		String body = """
				{"name": "payments-api", "description": "Handles card payments", "version": "1.4.2"}
				""";

		mockMvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesRegex(".*/api/services/\\d+")))
				.andExpect(jsonPath("$.name").value("payments-api"))
				.andExpect(jsonPath("$.description").value("Handles card payments"))
				.andExpect(jsonPath("$.version").value("1.4.2"))
				.andExpect(jsonPath("$.status").value("UNKNOWN"))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.updatedAt").isNotEmpty());

		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void getAllReturnsServicesSortedByName() throws Exception {
		repository.save(new ManagedService("payments-api", null, "1.4.2", ServiceStatus.HEALTHY));
		repository.save(new ManagedService("analytics-service", null, "1.8.3", ServiceStatus.DEGRADED));

		mockMvc.perform(get("/api/services"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("analytics-service"))
				.andExpect(jsonPath("$[0].status").value("DEGRADED"))
				.andExpect(jsonPath("$[1].name").value("payments-api"));
	}

	@Test
	void getByIdReturnsService() throws Exception {
		ManagedService saved = repository.save(
				new ManagedService("email-worker", "Sends emails", "3.0.1", ServiceStatus.FAILED));

		mockMvc.perform(get("/api/services/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(saved.getId()))
				.andExpect(jsonPath("$.name").value("email-worker"))
				.andExpect(jsonPath("$.status").value("FAILED"));
	}

	@Test
	void getByIdReturns404WhenServiceDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/services/{id}", 999_999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Managed service not found"));
	}

	@Test
	void createReturns400WhenRequestIsInvalid() throws Exception {
		String body = """
				{"name": "Not A Valid Name!", "version": ""}
				""";

		mockMvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.name").exists())
				.andExpect(jsonPath("$.errors.version").value("must not be blank"));

		assertThat(repository.count()).isZero();
	}

	@Test
	void createReturns409WhenNameAlreadyExists() throws Exception {
		repository.save(new ManagedService("payments-api", null, "1.4.2", ServiceStatus.HEALTHY));
		String body = """
				{"name": "payments-api", "version": "2.0.0"}
				""";

		mockMvc.perform(post("/api/services").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict());

		assertThat(repository.count()).isEqualTo(1);
	}

}
