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
 * One entry in the operational event timeline, mapped to the service_events table.
 */
@Entity
@Table(name = "service_events")
public class ServiceEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ManagedService service;

	@Column(name = "deployment_id")
	private Long deploymentId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ServiceEventType type;

	@Column(nullable = false, length = 500)
	private String summary;

	@Column(nullable = false, length = 250)
	private String topic;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	/** Required by JPA; not for application code. */
	protected ServiceEvent() {
	}

	public ServiceEvent(ManagedService service, Long deploymentId, ServiceEventType type, String summary, String topic) {
		this.service = service;
		this.deploymentId = deploymentId;
		this.type = type;
		this.summary = summary;
		this.topic = topic;
		this.occurredAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
	}

	public Long getId() {
		return id;
	}

	public ManagedService getService() {
		return service;
	}

	public Long getDeploymentId() {
		return deploymentId;
	}

	public ServiceEventType getType() {
		return type;
	}

	public String getSummary() {
		return summary;
	}

	public String getTopic() {
		return topic;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

}
