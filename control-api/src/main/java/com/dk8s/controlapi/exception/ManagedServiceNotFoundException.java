package com.dk8s.controlapi.exception;

public class ManagedServiceNotFoundException extends RuntimeException {

	public ManagedServiceNotFoundException(Long id) {
		super("Managed service " + id + " was not found");
	}

}
