package io.bharat.mongo.employee.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import io.bharat.mongo.employee.model.Employee;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, String id);
}

