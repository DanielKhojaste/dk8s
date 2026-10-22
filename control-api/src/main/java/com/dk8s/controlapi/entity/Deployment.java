package com.dk8s.controlapi.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A request to deploy a specific version of a managed service, mapped to the deployments table.
 */
@Entity
@Table(name = "deployments")
public class Deployment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Many deployments belong to one service (deployments.service_id -> managed_services.id). */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ManagedService service;

	@Column(nullable = false, length = 50)
	private String version;

	@Column(name = "previous_version", nullable = false, length = 50)
	private String previousVersion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private DeploymentStatus status;

	@Column(name = "failure_reason", length = 500)
	private String failureReason;

	@Column(name = "requested_at", nullable = false, updatable = false)
	private Instant requestedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	/** Required by JPA; not for application code. */
	protected Deployment() {
	}

	public Deployment(ManagedService service, String version) {
		this.service = service;
		this.version = version;
		this.previousVersion = service.getVersion();
		this.status = DeploymentStatus.PENDING;
		this.requestedAt = now();
	}

	public void markSucceeded() {
		this.status = DeploymentStatus.SUCCEEDED;
		this.completedAt = now();
	}

	public void markFailed(String reason) {
		this.status = DeploymentStatus.FAILED;
		this.failureReason = reason;
		this.completedAt = now();
	}

	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MICROS);
	}

	public Long getId() {
		return id;
	}

	public ManagedService getService() {
		return service;
	}

	public String getVersion() {
		return version;
	}

	public String getPreviousVersion() {
		return previousVersion;
	}

	public DeploymentStatus getStatus() {
		return status;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

}
