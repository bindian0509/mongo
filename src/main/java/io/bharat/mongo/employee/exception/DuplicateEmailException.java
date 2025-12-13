package io.bharat.mongo.employee.exception;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("Employee email already in use: " + email);
	}
}

