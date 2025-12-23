package io.bharat.mongo.testsupport;

import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.bharat.mongo.security.dto.TokenPairResponse;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestMongoConfig.class)
@TestPropertySource(properties = {
		"spring.docker.compose.enabled=false",
		"spring.docker.compose.skip.in-tests=true"
})
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
public abstract class BaseApiTest {

	@LocalServerPort
	private int port;

	protected final TestEnvironment env = TestEnvironment.load();

	protected RequestSpecification baseSpec;
	protected RequestSpecification authSpec;
	protected EmployeeDataHelper employees;

	@BeforeAll
	void setupRestAssured() {
		RestAssuredSupport.configureObjectMapper();
		enableLoggingOfRequestAndResponseIfValidationFails();

		String baseUrl = env.resolveBaseUrl(port);
		baseSpec = RestAssuredSupport.baseSpec(baseUrl);

		AuthClient authClient = new AuthClient(baseSpec, env);
		TokenPairResponse tokens = authClient.login();

		authSpec = new RequestSpecBuilder()
				.setBaseUri(baseUrl)
				.setContentType(ContentType.JSON)
				.addHeader(HttpHeaders.AUTHORIZATION, tokens.tokenType() + " " + tokens.accessToken())
				.build();

		employees = new EmployeeDataHelper(authSpec, env);
	}

	@AfterEach
	void cleanupEmployees() {
		employees.cleanup();
	}
}

