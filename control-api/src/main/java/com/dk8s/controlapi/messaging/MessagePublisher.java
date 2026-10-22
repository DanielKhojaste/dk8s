package com.dk8s.controlapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.dk8s.controlapi.exception.MessageBrokerUnavailableException;

import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes JSON messages to Solace topics as persistent (guaranteed) messages.
 */
@Component
public class MessagePublisher {

	private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

	private final JmsTemplate topicJmsTemplate;
	private final JsonMapper jsonMapper;

	public MessagePublisher(JmsTemplate topicJmsTemplate, JsonMapper jsonMapper) {
		this.topicJmsTemplate = topicJmsTemplate;
		this.jsonMapper = jsonMapper;
	}

	public void publish(String topic, Object payload) {
		String json = jsonMapper.writeValueAsString(payload);
		try {
			topicJmsTemplate.send(topic, session -> session.createTextMessage(json));
			log.info("Published to {}: {}", topic, json);
		}
		catch (JmsException ex) {
			throw new MessageBrokerUnavailableException(ex);
		}
	}

}
