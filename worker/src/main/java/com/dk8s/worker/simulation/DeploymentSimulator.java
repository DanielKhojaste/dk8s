package com.dk8s.worker.simulation;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Pretends to deploy a version. It doesn't install anything: it waits, then succeeds,
 * unless the version contains "fail" (e.g. "1.6.0-fail"), so the failure path can be demonstrated.
 */
@Component
public class DeploymentSimulator {

	private final Duration duration;

	public DeploymentSimulator(@Value("${dk8s.simulation.duration}") Duration duration) {
		this.duration = duration;
	}

	/** @return empty if the deployment succeeded, otherwise the failure reason. */
	public Optional<String> deploy(String serviceName, String version) {
		sleep(duration);
		if (version.toLowerCase().contains("fail")) {
			return Optional.of("Simulated failure: " + serviceName + " " + version + " did not pass its health checks");
		}
		return Optional.empty();
	}

	private static void sleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while simulating a deployment", ex);
		}
	}

}
