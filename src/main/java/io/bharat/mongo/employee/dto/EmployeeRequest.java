package io.bharat.mongo.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record EmployeeRequest(
		@NotBlank(message = "firstName is required") String firstName,
		@NotBlank(message = "lastName is required") String lastName,
		@NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
		@NotBlank(message = "department is required") String department,
		@NotBlank(message = "jobTitle is required") String jobTitle,
		@PositiveOrZero(message = "salary must be zero or positive") BigDecimal salary,
		@PastOrPresent(message = "dateOfJoining cannot be in the future") LocalDate dateOfJoining) {
}

