package com.dk8s.controlapi.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dk8s.controlapi.dto.CreateManagedServiceRequest;
import com.dk8s.controlapi.dto.ManagedServiceResponse;
import com.dk8s.controlapi.entity.ManagedService;
import com.dk8s.controlapi.entity.ServiceStatus;
import com.dk8s.controlapi.exception.DuplicateManagedServiceNameException;
import com.dk8s.controlapi.exception.ManagedServiceNotFoundException;
import com.dk8s.controlapi.repository.ManagedServiceRepository;

/**
 * Business logic for the service registry.
 */
@Service
public class ManagedServiceService {

	private final ManagedServiceRepository repository;

	public ManagedServiceService(ManagedServiceRepository repository) {
		this.repository = repository;
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

		return ManagedServiceResponse.from(repository.save(service));
	}

}
