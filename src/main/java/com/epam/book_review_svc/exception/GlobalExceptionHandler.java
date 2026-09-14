package com.epam.book_review_svc.exception;

import com.epam.book_review_svc.model.dto.ErrorResponse;
import com.epam.book_review_svc.model.dto.FieldError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(404)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(403)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.warn("Forbidden access: {}", ex.getMessage());
        return ResponseEntity.status(403).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(409)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(409).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .errors(ex.getFieldErrors())
                .build();

        log.warn("Validation failed with {} errors", ex.getFieldErrors().size());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(
            DataAccessException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .message("Internal server error")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.error("Data access error", ex);
        return ResponseEntity.status(500).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .status(400)
                .message("Validation failed")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .errors(fieldErrors)
                .build();

        log.warn("Bean validation failed with {} errors", fieldErrors.size());
        return ResponseEntity.status(400).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .status(500)
                .message("Internal server error")
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        log.error("Unexpected error", ex);
        return ResponseEntity.status(500).body(error);
    }
}

