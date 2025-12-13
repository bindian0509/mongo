package io.bharat.mongo.employee.api;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.bharat.mongo.employee.dto.EmployeeRequest;
import io.bharat.mongo.employee.dto.EmployeeResponse;
import io.bharat.mongo.employee.service.EmployeeService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

	private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public List<EmployeeResponse> listEmployees() {
		log.info("HTTP GET /api/employees");
		return employeeService.findAll();
	}

	@GetMapping("/{id}")
	public EmployeeResponse getEmployee(@PathVariable String id) {
		log.info("HTTP GET /api/employees/{}", id);
		return employeeService.findById(id);
	}

	@PostMapping
	public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
		log.info("HTTP POST /api/employees email={}", request.email());
		EmployeeResponse created = employeeService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	public EmployeeResponse updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeRequest request) {
		log.info("HTTP PUT /api/employees/{}", id);
		return employeeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
		log.info("HTTP DELETE /api/employees/{}", id);
		employeeService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

