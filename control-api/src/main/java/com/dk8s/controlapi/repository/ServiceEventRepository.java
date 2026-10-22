package com.dk8s.controlapi.repository;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dk8s.controlapi.entity.ServiceEvent;

public interface ServiceEventRepository extends JpaRepository<ServiceEvent, Long> {

	// @EntityGraph loads each event's service in the same query (a JOIN),
	// instead of one extra query per event when we read the service name.
	@EntityGraph(attributePaths = "service")
	List<ServiceEvent> findByOrderByOccurredAtDescIdDesc(Limit limit);

	@EntityGraph(attributePaths = "service")
	List<ServiceEvent> findByServiceIdOrderByOccurredAtDescIdDesc(Long serviceId, Limit limit);

}
