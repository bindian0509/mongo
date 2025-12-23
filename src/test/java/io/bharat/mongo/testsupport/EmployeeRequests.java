package io.bharat.mongo.testsupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.bharat.mongo.employee.dto.EmployeeRequest;

public final class EmployeeRequests {

	private EmployeeRequests() {
	}

	public static EmployeeRequest randomEmployee() {
		String unique = UUID.randomUUID().toString().substring(0, 8);
		return new EmployeeRequest(
				"First" + unique,
				"Last" + unique,
				employeeEmail(unique),
				"Engineering",
				"Developer",
				new BigDecimal("90000.00"),
				LocalDate.now().minusDays(30));
	}

	public static EmployeeRequest withEmail(String email) {
		return new EmployeeRequest(
				"First" + UUID.randomUUID().toString().substring(0, 4),
				"Last" + UUID.randomUUID().toString().substring(0, 4),
				email,
				"Engineering",
				"Developer",
				new BigDecimal("90000.00"),
				LocalDate.now().minusDays(30));
	}

	public static EmployeeRequest invalidEmail() {
		EmployeeRequest base = randomEmployee();
		return new EmployeeRequest(
				base.firstName(),
				base.lastName(),
				"not-an-email",
				base.department(),
				base.jobTitle(),
				base.salary(),
				base.dateOfJoining());
	}

	public static EmployeeRequest missingFirstName() {
		EmployeeRequest base = randomEmployee();
		return new EmployeeRequest(
				"",
				base.lastName(),
				base.email(),
				base.department(),
				base.jobTitle(),
				base.salary(),
				base.dateOfJoining());
	}

	private static String employeeEmail(String unique) {
		return "user-" + unique.toLowerCase() + "@example.com";
	}
}

