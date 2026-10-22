package com.dk8s.controlapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;

@Configuration
public class JmsConfig {

	/**
	 * JmsTemplate for publishing. pubSubDomain=true makes destination names mean Solace
	 * <em>topics</em> (e.g. dk8s/deployment/requested/payments-api), not queues.
	 * Listeners still read from queues, using Spring Boot's default listener settings.
	 */
	@Bean
	JmsTemplate topicJmsTemplate(ConnectionFactory connectionFactory) {
		// Reuse one broker connection instead of opening a new one for every message.
		JmsTemplate template = new JmsTemplate(new CachingConnectionFactory(connectionFactory));
		template.setPubSubDomain(true);
		return template;
	}

}
