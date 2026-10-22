package com.dk8s.worker.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class DeploymentSimulatorTests {

	private final DeploymentSimulator simulator = new DeploymentSimulator(Duration.ZERO);

	@Test
	void normalVersionSucceeds() {
		assertThat(simulator.deploy("payments-api", "1.5.0")).isEmpty();
	}

	@Test
	void versionContainingFailFails() {
		assertThat(simulator.deploy("payments-api", "1.6.0-FAIL"))
				.hasValueSatisfying(reason -> assertThat(reason).contains("payments-api 1.6.0-FAIL"));
	}

}
