package com.example.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
