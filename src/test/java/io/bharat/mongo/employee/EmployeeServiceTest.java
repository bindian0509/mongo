package io.bharat.mongo.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.bharat.mongo.employee.exception.DuplicateEmailException;
import io.bharat.mongo.employee.exception.NotFoundException;
import io.bharat.mongo.employee.model.Employee;
import io.bharat.mongo.employee.repository.EmployeeRepository;
import io.bharat.mongo.employee.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

	@Mock
	private EmployeeRepository repository;

	@InjectMocks
	private EmployeeService service;

	@Test
	void createEmployee_succeeds_whenEmailUnique() {
		EmployeeRequest request = sampleRequest();
		when(repository.existsByEmail(anyString())).thenReturn(false);
		when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
			Employee e = invocation.getArgument(0);
			e.setId("abc123");
			return e;
		});

		EmployeeResponse response = service.create(request);

		assertThat(response.id()).isEqualTo("abc123");
		assertThat(response.email()).isEqualTo("jane.doe@example.com");
		verify(repository).save(any(Employee.class));
	}

	@Test
	void createEmployee_throws_whenEmailNotUnique() {
		EmployeeRequest request = sampleRequest();
		when(repository.existsByEmail(anyString())).thenReturn(true);

		assertThatThrownBy(() -> service.create(request))
				.isInstanceOf(DuplicateEmailException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void updateEmployee_updatesFields_whenFound() {
		Employee existing = sampleEmployee();
		EmployeeRequest request = new EmployeeRequest("Jane", "Roe", "jane.roe@example.com", "Platform",
				"Staff Engineer", BigDecimal.valueOf(150000), LocalDate.of(2023, 1, 15));

		when(repository.findById("emp1")).thenReturn(Optional.of(existing));
		when(repository.existsByEmailAndIdNot("jane.roe@example.com", "emp1")).thenReturn(false);
		when(repository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		EmployeeResponse response = service.update("emp1", request);

		assertThat(response.id()).isEqualTo("emp1");
		assertThat(response.lastName()).isEqualTo("Roe");
		assertThat(response.email()).isEqualTo("jane.roe@example.com");
	}

	@Test
	void updateEmployee_throws_whenEmailUsedByAnother() {
		Employee existing = sampleEmployee();
		EmployeeRequest request = sampleRequest();

		when(repository.findById("emp1")).thenReturn(Optional.of(existing));
		when(repository.existsByEmailAndIdNot("jane.doe@example.com", "emp1")).thenReturn(true);

		assertThatThrownBy(() -> service.update("emp1", request))
				.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	void findById_throws_whenMissing() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById("missing"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void delete_throws_whenMissing() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete("missing"))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void list_returnsMappedEmployees() {
		when(repository.findAll()).thenReturn(List.of(sampleEmployee()));

		List<EmployeeResponse> employees = service.findAll();

		assertThat(employees).hasSize(1);
		assertThat(employees.get(0).firstName()).isEqualTo("Jane");
	}

	private EmployeeRequest sampleRequest() {
		return new EmployeeRequest("Jane", "Doe", "jane.doe@example.com", "Engineering", "Backend Engineer",
				BigDecimal.valueOf(120000), LocalDate.of(2023, 1, 15));
	}

	private Employee sampleEmployee() {
		return new Employee("emp1", "Jane", "Doe", "jane.doe@example.com", "Engineering", "Backend Engineer",
				BigDecimal.valueOf(120000), LocalDate.of(2023, 1, 15));
	}
}

