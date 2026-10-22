package com.dk8s.controlapi.exception;

public class DeploymentInProgressException extends RuntimeException {

	public DeploymentInProgressException(String serviceName) {
		super("A deployment of '" + serviceName + "' is already in progress");
	}

}
