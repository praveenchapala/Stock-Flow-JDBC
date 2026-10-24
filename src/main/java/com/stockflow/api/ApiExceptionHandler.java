
package com.stockflow.api;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(
                Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException exception) {

        String message = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Invalid request parameter.");

        return ResponseEntity.badRequest().body(
                Map.of("error", message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "error",
                        "Invalid value for parameter: " + exception.getName()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request.");

        return ResponseEntity.badRequest().body(
                Map.of("error", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "error",
                        "Request body is missing or contains invalid JSON."
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            NoResourceFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "error", "RESOURCE_NOT_FOUND",
                        "message", "The requested resource was not found."
                ));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseError(
            SQLException exception) {

        logger.error("Database operation failed.", exception);

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "error",
                        "A database error occurred. Please try again later."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedError(
            Exception exception) {

        logger.error("Unexpected API error occurred.", exception);

        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "error",
                        "An unexpected server error occurred."
                ));
    }
}
