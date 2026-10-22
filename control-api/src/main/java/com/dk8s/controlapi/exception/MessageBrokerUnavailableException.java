package com.dk8s.controlapi.exception;

public class MessageBrokerUnavailableException extends RuntimeException {

	public MessageBrokerUnavailableException(Throwable cause) {
		super("The message broker (Solace PubSub+) is unavailable", cause);
	}

}
