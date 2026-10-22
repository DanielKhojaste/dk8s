package com.dk8s.controlapi.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dk8s.controlapi.dto.CreateManagedServiceRequest;
import com.dk8s.controlapi.dto.ManagedServiceResponse;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceEventType;
import com.dk8s.controlapi.entity.ServiceStatus;
import com.dk8s.controlapi.exception.DuplicateManagedServiceNameException;
import com.dk8s.controlapi.exception.ManagedServiceNotFoundException;
import com.dk8s.controlapi.messaging.MessagePublisher;
import com.dk8s.controlapi.messaging.ServiceRegisteredMessage;
import com.dk8s.controlapi.messaging.Topics;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Business logic for the service registry.
 */
@Service
public class ManagedServiceService {

	private final ManagedServiceRepository repository;
	private final ServiceEventService eventService;
	private final MessagePublisher publisher;

	public ManagedServiceService(ManagedServiceRepository repository, ServiceEventService eventService,
			MessagePublisher publisher) {
		this.repository = repository;
		this.eventService = eventService;
		this.publisher = publisher;
	}

	@Transactional(readOnly = true)
	public List<ManagedServiceResponse> findAll() {
		return repository.findAll(Sort.by("name"))
				.stream()
				.map(ManagedServiceResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ManagedServiceResponse findById(Long id) {
		return repository.findById(id)
				.map(ManagedServiceResponse::from)
				.orElseThrow(() -> new ManagedServiceNotFoundException(id));
	}

	@Transactional
	public ManagedServiceResponse create(CreateManagedServiceRequest request) {
		if (repository.existsByName(request.name())) {
			throw new DuplicateManagedServiceNameException(request.name());
		}

		ManagedService service = new ManagedService(
				request.name(),
				request.description(),
				request.version(),
				ServiceStatus.UNKNOWN);

		ManagedService saved = repository.save(service);
		String topic = Topics.serviceRegistered(saved.getName());
		eventService.record(saved, null, ServiceEventType.SERVICE_REGISTERED,
				saved.getName() + " registered at version " + saved.getVersion(), topic);
		publisher.publish(topic, ServiceRegisteredMessage.from(saved)); // last: a broker failure rolls everything back

		return ManagedServiceResponse.from(saved);
	}

}
