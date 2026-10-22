package com.dk8s.controlapi.exception;

public class DuplicateManagedServiceNameException extends RuntimeException {

	public DuplicateManagedServiceNameException(String name) {
		super("A managed service named '" + name + "' already exists");
	}

}
