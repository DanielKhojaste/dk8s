package com.dk8s.controlapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.dk8s.controlapi.messaging.MessagePublisher;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class ControlApiApplicationTests {

	@MockitoBean
	private MessagePublisher publisher;

	@Test
	void contextLoads() {
	}

}
