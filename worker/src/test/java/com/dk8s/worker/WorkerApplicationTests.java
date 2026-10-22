package com.dk8s.worker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // don't connect the JMS listener to a real broker
class WorkerApplicationTests {

	@Test
	void contextLoads() {
	}

}
