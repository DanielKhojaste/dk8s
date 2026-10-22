package com.dk8s.worker.messaging;

/**
 * Builds DK8s topic names. Solace topics are hierarchical, separated by "/":
 *
 * <pre>
 * dk8s / deployment / requested / payments-api
 * app    entity       action      service name
 * </pre>
 *
 * Queues subscribe with wildcards, e.g. "dk8s/deployment/requested/&gt;" matches every service.
 */
public final class Topics {

	private Topics() {
	}

	public static String deployment(DeploymentMessageType type, String serviceName) {
		return "dk8s/deployment/" + type.name().toLowerCase() + "/" + serviceName;
	}

}
