package com.dk8s.controlapi.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Turns exceptions thrown by controllers into consistent JSON error responses
 * (RFC 9457 "problem details").
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ManagedServiceNotFoundException.class)
	ProblemDetail handleNotFound(ManagedServiceNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Managed service not found", ex.getMessage());
	}

	@ExceptionHandler(DuplicateManagedServiceNameException.class)
	ProblemDetail handleDuplicateName(DuplicateManagedServiceNameException ex) {
		return problem(HttpStatus.CONFLICT, "Duplicate managed service name", ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}

		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed",
				"One or more fields are invalid");
		problem.setProperty("errors", errors);
		return problem;
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		return problem;
	}

}
