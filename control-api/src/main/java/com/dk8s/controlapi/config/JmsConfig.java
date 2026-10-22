package com.dk8s.controlapi.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.client.RestClient;

import com.dk8s.controlapi.messaging.BrokerMonitor;

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

	/** Read-only client for the broker's SEMP v2 management API (queue statistics). */
	@Bean
	BrokerMonitor brokerMonitor(
			@Value("${dk8s.solace.semp-url}") String sempUrl,
			@Value("${dk8s.solace.admin-username}") String adminUsername,
			@Value("${dk8s.solace.admin-password}") String adminPassword,
			@Value("${solace.jms.msg-vpn}") String msgVpn) {
		// Short timeouts: the dashboard asks every few seconds, and a stuck broker shouldn't stall it.
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(2));

		RestClient sempClient = RestClient.builder()
				.requestFactory(requestFactory)
				.baseUrl(sempUrl)
				.defaultHeaders(headers -> headers.setBasicAuth(adminUsername, adminPassword))
				.build();
		return new BrokerMonitor(sempClient, msgVpn);
	}

}
