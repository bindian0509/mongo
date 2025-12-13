package io.bharat.mongo.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class EmployeeControllerValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validRequest_hasNoViolations() {
		EmployeeRequest request = new EmployeeRequest("Jane", "Doe", "jane.doe@example.com", "Engineering",
				"Backend Engineer", BigDecimal.valueOf(120000), LocalDate.of(2023, 1, 15));

		Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(request);

		assertThat(violations).isEmpty();
	}

	@Test
	void missingFirstName_triggersViolation() {
		EmployeeRequest request = new EmployeeRequest("", "Doe", "jane.doe@example.com", "Engineering",
				"Backend Engineer", BigDecimal.valueOf(120000), LocalDate.of(2023, 1, 15));

		Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(request);

		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
	}

	@Test
	void invalidEmail_triggersViolation() {
		EmployeeRequest request = new EmployeeRequest("Jane", "Doe", "invalid-email", "Engineering",
				"Backend Engineer", BigDecimal.valueOf(120000), LocalDate.of(2023, 1, 15));

		Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(request);

		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
	}

	@Test
	void negativeSalary_triggersViolation() {
		EmployeeRequest request = new EmployeeRequest("Jane", "Doe", "jane.doe@example.com", "Engineering",
				"Backend Engineer", BigDecimal.valueOf(-10), LocalDate.of(2023, 1, 15));

		Set<ConstraintViolation<EmployeeRequest>> violations = validator.validate(request);

		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("salary"));
	}
}

