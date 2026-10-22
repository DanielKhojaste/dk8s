package com.dk8s.controlapi.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dk8s.controlapi.entity.Deployment;
import com.dk8s.controlapi.entity.DeploymentStatus;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

	List<Deployment> findByServiceIdOrderByRequestedAtDescIdDesc(Long serviceId);

	Optional<Deployment> findByIdAndServiceId(Long id, Long serviceId);

	boolean existsByServiceIdAndStatusIn(Long serviceId, Collection<DeploymentStatus> statuses);

}
