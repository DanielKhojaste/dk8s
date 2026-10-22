package com.dk8s.controlapi.service;

import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dk8s.controlapi.dto.CreateDeploymentRequest;
import com.dk8s.controlapi.dto.DeploymentResponse;
import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.DeploymentStatus;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceEventType;
import com.dk8s.controlapi.exception.DeploymentInProgressException;
import com.dk8s.controlapi.exception.DeploymentNotFoundException;
import com.dk8s.controlapi.exception.ManagedServiceNotFoundException;
import com.dk8s.controlapi.messaging.DeploymentMessage;
import com.dk8s.controlapi.messaging.DeploymentMessageType;
import com.dk8s.controlapi.messaging.MessagePublisher;
import com.dk8s.controlapi.messaging.Topics;
import com.dk8s.controlapi.repository.DeploymentRepository;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Business logic for deployments.
 *
 * <ol>
 * <li>{@link #requestDeployment}: save the deployment as PENDING and publish REQUESTED to Solace.</li>
 * <li>The worker does the work and publishes STARTED, then COMPLETED or FAILED.</li>
 * <li>{@link #applyUpdate}: apply each of those messages to the database.</li>
 * </ol>
 */
@Service
public class DeploymentService {

	private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);

	private static final EnumSet<DeploymentStatus> ACTIVE_STATUSES =
			EnumSet.of(DeploymentStatus.PENDING, DeploymentStatus.IN_PROGRESS);

	private final DeploymentRepository deploymentRepository;
	private final ManagedServiceRepository serviceRepository;
	private final ServiceEventService eventService;
	private final MessagePublisher publisher;

	public DeploymentService(DeploymentRepository deploymentRepository, ManagedServiceRepository serviceRepository,
			ServiceEventService eventService, MessagePublisher publisher) {
		this.deploymentRepository = deploymentRepository;
		this.serviceRepository = serviceRepository;
		this.eventService = eventService;
		this.publisher = publisher;
	}

	@Transactional(readOnly = true)
	public List<DeploymentResponse> findByService(Long serviceId) {
		requireService(serviceId);
		return deploymentRepository.findByServiceIdOrderByRequestedAtDescIdDesc(serviceId)
				.stream()
				.map(DeploymentResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DeploymentResponse findById(Long serviceId, Long deploymentId) {
		return deploymentRepository.findByIdAndServiceId(deploymentId, serviceId)
				.map(DeploymentResponse::from)
				.orElseThrow(() -> new DeploymentNotFoundException(serviceId, deploymentId));
	}

	@Transactional
	public DeploymentResponse requestDeployment(Long serviceId, CreateDeploymentRequest request) {
		ManagedService service = requireService(serviceId);

		if (deploymentRepository.existsByServiceIdAndStatusIn(serviceId, ACTIVE_STATUSES)) {
			throw new DeploymentInProgressException(service.getName());
		}

		Deployment deployment = deploymentRepository.save(new Deployment(service, request.version()));
		String topic = Topics.deployment(DeploymentMessageType.REQUESTED, service.getName());
		eventService.record(service, deployment.getId(), ServiceEventType.DEPLOYMENT_REQUESTED,
				"Deployment of " + deployment.getVersion() + " requested", topic);

		// Published last, still inside the transaction: if Solace is down this throws,
		// the transaction rolls back, and no PENDING deployment is left behind.
		publisher.publish(topic, DeploymentMessage.requested(deployment));

		return DeploymentResponse.from(deployment);
	}

	/**
	 * Applies a STARTED / COMPLETED / FAILED message from the worker.
	 * Messages for unknown or already-finished deployments are ignored, so a redelivered
	 * (duplicate) message can't change anything twice.
	 */
	@Transactional
	public void applyUpdate(DeploymentMessage update) {
		Deployment deployment = deploymentRepository.findById(update.deploymentId()).orElse(null);
		if (deployment == null) {
			log.warn("Ignoring {} for unknown deployment {}", update.type(), update.deploymentId());
			return;
		}
		if (!deployment.getStatus().isActive()) {
			log.info("Ignoring {} for deployment {}: already {}", update.type(), deployment.getId(), deployment.getStatus());
			return;
		}

		ManagedService service = deployment.getService();
		String topic = Topics.deployment(update.type(), service.getName());

		switch (update.type()) {
			case STARTED -> {
				deployment.markInProgress();
				eventService.record(service, deployment.getId(), ServiceEventType.DEPLOYMENT_STARTED,
						"Worker started deploying " + deployment.getVersion(), topic);
			}
			case COMPLETED -> {
				deployment.markSucceeded();
				service.applyDeployedVersion(deployment.getVersion());
				eventService.record(service, deployment.getId(), ServiceEventType.DEPLOYMENT_COMPLETED,
						service.getName() + " is now running " + deployment.getVersion(), topic);
			}
			case FAILED -> {
				deployment.markFailed(update.failureReason());
				service.markDegraded();
				eventService.record(service, deployment.getId(), ServiceEventType.DEPLOYMENT_FAILED,
						"Deployment of " + deployment.getVersion() + " failed: " + update.failureReason(), topic);
			}
			case REQUESTED -> log.warn("Ignoring unexpected REQUESTED update for deployment {}", deployment.getId());
		}
	}

	private ManagedService requireService(Long serviceId) {
		return serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ManagedServiceNotFoundException(serviceId));
	}

}
