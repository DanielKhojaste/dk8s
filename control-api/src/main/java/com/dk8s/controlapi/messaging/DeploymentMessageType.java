package com.dk8s.controlapi.messaging;

/** What happened to a deployment. Each type is published to its own topic level. */
public enum DeploymentMessageType {
	REQUESTED,
	STARTED,
	COMPLETED,
	FAILED
}
