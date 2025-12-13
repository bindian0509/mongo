package io.bharat.mongo.employee.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.bharat.mongo.employee.exception.DuplicateEmailException;
import io.bharat.mongo.employee.exception.NotFoundException;
import io.bharat.mongo.employee.model.Employee;
import io.bharat.mongo.employee.repository.EmployeeRepository;

@Service
public class EmployeeService {

	private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

	private final EmployeeRepository repository;

	public EmployeeService(EmployeeRepository repository) {
		this.repository = repository;
	}

	public List<EmployeeResponse> findAll() {
		log.info("Fetching all employees");
		List<EmployeeResponse> employees = repository.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
		log.info("Fetched {} employees", employees.size());
		return employees;
	}

	public EmployeeResponse findById(String id) {
		log.info("Fetching employee with id={}", id);
		return toResponse(fetchEmployee(id));
	}

	public EmployeeResponse create(EmployeeRequest request) {
		log.info("Creating employee with email={}", request.email());
		String normalizedEmail = normalizeEmail(request.email());
		validateEmailUniqueness(normalizedEmail, null);

		Employee employee = new Employee();
		applyRequest(employee, request, normalizedEmail);

		Employee saved = repository.save(employee);
		log.info("Created employee id={}", saved.getId());
		return toResponse(saved);
	}

	public EmployeeResponse update(String id, EmployeeRequest request) {
		log.info("Updating employee id={}", id);
		Employee employee = fetchEmployee(id);
		String normalizedEmail = normalizeEmail(request.email());
		validateEmailUniqueness(normalizedEmail, id);

		applyRequest(employee, request, normalizedEmail);

		Employee updated = repository.save(employee);
		log.info("Updated employee id={}", updated.getId());
		return toResponse(updated);
	}

	public void delete(String id) {
		log.info("Deleting employee id={}", id);
		Employee employee = fetchEmployee(id);
		repository.delete(employee);
		log.info("Deleted employee id={}", id);
	}

	private Employee fetchEmployee(String id) {
		return repository.findById(id)
				.orElseThrow(() -> {
					log.warn("Employee not found id={}", id);
					return new NotFoundException("Employee not found: " + id);
				});
	}

	private void validateEmailUniqueness(String email, String currentId) {
		boolean emailExists = currentId == null
				? repository.existsByEmail(email)
				: repository.existsByEmailAndIdNot(email, currentId);

		if (emailExists) {
			log.warn("Email already in use email={} currentId={}", email, currentId);
			throw new DuplicateEmailException(email);
		}
	}

	private void applyRequest(Employee employee, EmployeeRequest request, String normalizedEmail) {
		employee.setFirstName(request.firstName());
		employee.setLastName(request.lastName());
		employee.setEmail(normalizedEmail);
		employee.setDepartment(request.department());
		employee.setJobTitle(request.jobTitle());
		employee.setSalary(request.salary());
		employee.setDateOfJoining(request.dateOfJoining());
	}

	private String normalizeEmail(String email) {
		return StringUtils.hasText(email) ? email.trim().toLowerCase() : email;
	}

	private EmployeeResponse toResponse(Employee employee) {
		return new EmployeeResponse(
				employee.getId(),
				employee.getFirstName(),
				employee.getLastName(),
				employee.getEmail(),
				employee.getDepartment(),
				employee.getJobTitle(),
				employee.getSalary(),
				employee.getDateOfJoining());
	}
}

