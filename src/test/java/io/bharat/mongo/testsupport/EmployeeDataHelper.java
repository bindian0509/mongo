package io.bharat.mongo.testsupport;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Creates and cleans up employee data via HTTP calls.
 */
public class EmployeeDataHelper {

	private static final Logger log = LoggerFactory.getLogger(EmployeeDataHelper.class);

	private final RequestSpecification authSpec;
	private final TestEnvironment env;
	private final List<String> createdEmployeeIds = new ArrayList<>();

	public EmployeeDataHelper(RequestSpecification authSpec, TestEnvironment env) {
		this.authSpec = authSpec;
		this.env = env;
	}

	public EmployeeResponse create(EmployeeRequest request) {
		EmployeeResponse response = given(authSpec)
				.contentType(ContentType.JSON)
				.body(request)
				.when()
				.post(env.employeesPath())
				.then()
				.statusCode(HttpStatus.CREATED.value())
				.extract()
				.as(EmployeeResponse.class);

		createdEmployeeIds.add(response.id());
		return response;
	}

	public void delete(String id) {
		Response response = given(authSpec)
				.when()
				.delete(env.employeesPath() + "/" + id);

		if (response.statusCode() == HttpStatus.NO_CONTENT.value()) {
			log.info("Deleted employee id={}", id);
		} else if (response.statusCode() == HttpStatus.NOT_FOUND.value()) {
			log.info("Employee already absent id={}", id);
		} else {
			log.warn("Unexpected status deleting employee id={} status={}", id, response.statusCode());
		}
		createdEmployeeIds.remove(id);
	}

	public void cleanup() {
		List<String> ids = new ArrayList<>(createdEmployeeIds);
		Collections.reverse(ids);
		for (String id : ids) {
			try {
				delete(id);
			} catch (Exception e) {
				log.warn("Cleanup failed for employee id={}", id, e);
			}
		}
		createdEmployeeIds.clear();
	}
}

