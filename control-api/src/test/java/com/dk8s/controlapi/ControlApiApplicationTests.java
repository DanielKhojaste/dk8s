package com.dk8s.controlapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ControlApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
