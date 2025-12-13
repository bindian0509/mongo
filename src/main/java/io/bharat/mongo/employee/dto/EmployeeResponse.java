package io.bharat.mongo.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
		String id,
		String firstName,
		String lastName,
		String email,
		String department,
		String jobTitle,
		BigDecimal salary,
		LocalDate dateOfJoining) {
}

