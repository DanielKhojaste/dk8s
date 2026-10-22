package com.dk8s.controlapi.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * A service registered in DK8s, mapped to the managed_services table.
 */
@Entity
@Table(name = "managed_services")
public class ManagedService {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 63)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(nullable = false, length = 50)
	private String version;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ServiceStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	/** Required by JPA; not for application code. */
	protected ManagedService() {
	}

	public ManagedService(String name, String description, String version, ServiceStatus status) {
		this.name = name;
		this.description = description;
		this.version = version;
		this.status = status;
	}

	@PrePersist
	void onCreate() {
		Instant now = now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = now();
	}

	/** Called when a deployment fails: the previous version keeps running, but something is wrong. */
	public void markDegraded() {
		this.status = ServiceStatus.DEGRADED;
	}

	/** Called when a deployment of this service succeeds. */
	public void applyDeployedVersion(String deployedVersion) {
		this.version = deployedVersion;
		this.status = ServiceStatus.HEALTHY;
	}

	/** PostgreSQL stores microseconds, so drop the extra precision before saving. */
	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MICROS);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}

	public ServiceStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
