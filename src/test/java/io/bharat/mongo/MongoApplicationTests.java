package io.bharat.mongo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.bharat.mongo.testsupport.TestMongoConfig;

@SpringBootTest
@Import(TestMongoConfig.class)
@TestPropertySource(properties = {
		"spring.docker.compose.enabled=false",
		"spring.docker.compose.skip.in-tests=true"
})
@ActiveProfiles("test")
class MongoApplicationTests {

	@Test
	void contextLoads() {
	}

}
