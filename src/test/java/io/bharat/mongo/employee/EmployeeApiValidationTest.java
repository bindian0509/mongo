package io.bharat.mongo.employee;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.bharat.mongo.testsupport.BaseApiTest;
import io.bharat.mongo.testsupport.EmployeeRequests;

class EmployeeApiValidationTest extends BaseApiTest {

	@Test
	void creating_with_invalid_email_returns_validation_error() {
		var invalid = EmployeeRequests.invalidEmail();

		given(authSpec)
				.body(invalid)
				.when()
				.post(env.employeesPath())
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.body("message", equalTo("Validation failed"))
				.body("validationErrors.email", equalTo("email must be valid"));
	}

	@Test
	void creating_with_missing_first_name_returns_validation_error() {
		var invalid = EmployeeRequests.missingFirstName();

		given(authSpec)
				.body(invalid)
				.when()
				.post(env.employeesPath())
				.then()
				.statusCode(HttpStatus.BAD_REQUEST.value())
				.body("message", equalTo("Validation failed"))
				.body("validationErrors.firstName", equalTo("firstName is required"));
	}

	@Test
	void creating_with_duplicate_email_returns_conflict() {
		var first = employees.create(EmployeeRequests.randomEmployee());
		var duplicateEmailRequest = EmployeeRequests.withEmail(first.email());

		given(authSpec)
				.body(duplicateEmailRequest)
				.when()
				.post(env.employeesPath())
				.then()
				.statusCode(HttpStatus.CONFLICT.value())
				.body("message", equalTo("Employee email already in use: " + first.email()));
	}
}

