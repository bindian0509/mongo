package io.bharat.mongo.employee;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.bharat.mongo.testsupport.BaseApiTest;
import io.bharat.mongo.testsupport.EmployeeRequests;

class EmployeeApiCrudTest extends BaseApiTest {

	@Test
	void create_read_update_delete_employee() {
		EmployeeRequest request = EmployeeRequests.randomEmployee();

		EmployeeResponse created = employees.create(request);
		assertThat(created.id()).isNotBlank();
		assertThat(created.email()).isEqualTo(request.email().toLowerCase());

		EmployeeResponse fetched = given(authSpec)
				.when()
				.get(env.employeesPath() + "/" + created.id())
				.then()
				.statusCode(HttpStatus.OK.value())
				.extract()
				.as(EmployeeResponse.class);
		assertThat(fetched.firstName()).isEqualTo(request.firstName());

		EmployeeRequest update = EmployeeRequests.withEmail("updated-" + request.email());
		EmployeeResponse updated = given(authSpec)
				.body(update)
				.when()
				.put(env.employeesPath() + "/" + created.id())
				.then()
				.statusCode(HttpStatus.OK.value())
				.extract()
				.as(EmployeeResponse.class);

		assertThat(updated.firstName()).isEqualTo(update.firstName());
		assertThat(updated.email()).isEqualTo(update.email().toLowerCase());

		employees.delete(created.id());

		given(authSpec)
				.when()
				.get(env.employeesPath() + "/" + created.id())
				.then()
				.statusCode(HttpStatus.NOT_FOUND.value())
				.body("message", equalTo("Employee not found: " + created.id()));
	}
}

