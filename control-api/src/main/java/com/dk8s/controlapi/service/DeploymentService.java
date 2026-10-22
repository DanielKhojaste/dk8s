package com.dk8s.controlapi.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dk8s.controlapi.dto.CreateDeploymentRequest;
import com.dk8s.controlapi.dto.DeploymentResponse;
import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.DeploymentStatus;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.exception.DeploymentInProgressException;
import com.dk8s.controlapi.exception.DeploymentNotFoundException;
import com.dk8s.controlapi.exception.ManagedServiceNotFoundException;
import com.dk8s.controlapi.repository.DeploymentRepository;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Business logic for deploying a new version of a managed service.
 */
@Service
public class DeploymentService {

	private static final EnumSet<DeploymentStatus> ACTIVE_STATUSES =
			EnumSet.of(DeploymentStatus.PENDING, DeploymentStatus.IN_PROGRESS);

	private final DeploymentRepository deploymentRepository;
	private final ManagedServiceRepository serviceRepository;

	public DeploymentService(DeploymentRepository deploymentRepository, ManagedServiceRepository serviceRepository) {
		this.deploymentRepository = deploymentRepository;
		this.serviceRepository = serviceRepository;
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

		// Simulated deployment: it completes immediately. Both entities are saved
		// automatically when the transaction commits (JPA "dirty checking").
		deployment.markSucceeded();
		service.applyDeployedVersion(deployment.getVersion());

		return DeploymentResponse.from(deployment);
	}

	private ManagedService requireService(Long serviceId) {
		return serviceRepository.findById(serviceId)
				.orElseThrow(() -> new ManagedServiceNotFoundException(serviceId));
	}

}
