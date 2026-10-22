package com.dk8s.controlapi.service;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dk8s.controlapi.dto.ServiceEventResponse;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceEvent;
import com.dk8s.controlapi.entity.ServiceEventType;
import com.dk8s.controlapi.exception.ManagedServiceNotFoundException;
import com.dk8s.controlapi.repository.ManagedServiceRepository;
import com.dk8s.controlapi.repository.ServiceEventRepository;

/**
 * Records and reads the operational event timeline.
 */
@Service
public class ServiceEventService {

	public static final int MAX_LIMIT = 100;

	private final ServiceEventRepository eventRepository;
	private final ManagedServiceRepository serviceRepository;

	public ServiceEventService(ServiceEventRepository eventRepository, ManagedServiceRepository serviceRepository) {
		this.eventRepository = eventRepository;
		this.serviceRepository = serviceRepository;
	}

	/** Called inside other services' transactions, so the event is saved together with the change it describes. */
	public void record(ManagedService service, Long deploymentId, ServiceEventType type, String summary, String topic) {
		eventRepository.save(new ServiceEvent(service, deploymentId, type, summary, topic));
	}

	@Transactional(readOnly = true)
	public List<ServiceEventResponse> findRecent(int limit) {
		return eventRepository.findByOrderByOccurredAtDescIdDesc(Limit.of(clamp(limit)))
				.stream()
				.map(ServiceEventResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ServiceEventResponse> findByService(Long serviceId, int limit) {
		if (!serviceRepository.existsById(serviceId)) {
			throw new ManagedServiceNotFoundException(serviceId);
		}
		return eventRepository.findByServiceIdOrderByOccurredAtDescIdDesc(serviceId, Limit.of(clamp(limit)))
				.stream()
				.map(ServiceEventResponse::from)
				.toList();
	}

	private static int clamp(int limit) {
		return Math.max(1, Math.min(limit, MAX_LIMIT));
	}

}
