package io.bharat.mongo.employee;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.bharat.mongo.testsupport.BaseApiTest;
import io.bharat.mongo.testsupport.EmployeeRequests;

class EmployeeApiListingTest extends BaseApiTest {

	@Test
	void list_returns_created_employees() {
		var first = employees.create(EmployeeRequests.randomEmployee());
		var second = employees.create(EmployeeRequests.randomEmployee());

		EmployeeResponse[] response = given(authSpec)
				.when()
				.get(env.employeesPath())
				.then()
				.statusCode(HttpStatus.OK.value())
				.extract()
				.as(EmployeeResponse[].class);

		List<String> ids = Arrays.stream(response)
				.map(EmployeeResponse::id)
				.collect(Collectors.toList());

		assertThat(ids).contains(first.id(), second.id());
		assertThat(response.length).isGreaterThanOrEqualTo(2);
	}
}

