package com.dk8s.controlapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dk8s.controlapi.entity.ManagedService;

public interface ManagedServiceRepository extends JpaRepository<ManagedService, Long> {

	boolean existsByName(String name);

}
