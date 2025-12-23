package io.bharat.mongo.testsupport;

import static io.restassured.RestAssured.given;

import org.springframework.http.HttpStatus;

import io.bharat.mongo.security.dto.LoginRequest;
import io.bharat.mongo.security.dto.TokenPairResponse;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/**
 * Lightweight helper to obtain JWTs for tests.
 */
public class AuthClient {

	private final RequestSpecification requestSpec;
	private final TestEnvironment env;

	public AuthClient(RequestSpecification requestSpec, TestEnvironment env) {
		this.requestSpec = requestSpec;
		this.env = env;
	}

	public TokenPairResponse login() {
		return performLogin().extract().as(TokenPairResponse.class);
	}

	private ValidatableResponse performLogin() {
		LoginRequest loginRequest = new LoginRequest(env.username(), env.password());

		return given(requestSpec)
				.contentType(ContentType.JSON)
				.body(loginRequest)
				.when()
				.post(env.loginPath())
				.then()
				.statusCode(HttpStatus.OK.value());
	}
}

