package io.bharat.mongo.employee.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		Map<String, String> validationErrors) {
}

