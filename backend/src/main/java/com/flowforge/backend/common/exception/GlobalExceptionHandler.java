package com.flowforge.backend.common.exception;

import com.flowforge.backend.common.enums.ErrorCode;
import com.flowforge.backend.common.response.ApiError;
import com.flowforge.backend.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handle(
        ResourceNotFoundException ex
    ) {

        return build(
            HttpStatus.NOT_FOUND,
            ErrorCode.RESOURCE_NOT_FOUND,
            ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handle(
        DuplicateResourceException ex
    ) {

        return build(
            HttpStatus.CONFLICT,
            ErrorCode.DUPLICATE_RESOURCE,
            ex.getMessage()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handle(
        UnauthorizedException ex
    ) {

        return build(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.UNAUTHORIZED,
            ex.getMessage()
        );
    }

    @ExceptionHandler(WorkflowRateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimit(
        WorkflowRateLimitExceededException ex
    ) {

        return build(
            HttpStatus.TOO_MANY_REQUESTS,
            ErrorCode.TOO_MANY_REQUESTS,
            ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
        MethodArgumentNotValidException ex
    ) {

        List<ApiError> errors =
            ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                    ApiError.builder()
                        .code("VALIDATION_ERROR")
                        .message(
                            error.getField()
                                + ": "
                                + error.getDefaultMessage()
                        )
                        .build()
                )
                .toList();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse.<Void>builder()
                    .success(false)
                    .timestamp(Instant.now())
                    .errors(errors)
                    .build()
            );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
        ConstraintViolationException ex
    ) {

        return build(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
        IllegalArgumentException ex
    ) {

        return build(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
        IllegalStateException ex
    ) {

        return build(
            HttpStatus.CONFLICT,
            "INVALID_STATE",
            ex.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(
        DataIntegrityViolationException ex
    ) {

        return build(
            HttpStatus.CONFLICT,
            "DATA_INTEGRITY_VIOLATION",
            "The request conflicts with existing data."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
        Exception ex
    ) {

        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred."
        );
    }

    private ResponseEntity<ApiResponse<Void>> build(
        HttpStatus status,
        ErrorCode code,
        String message
    ) {

        return build(
            status,
            code.name(),
            message
        );
    }

    private ResponseEntity<ApiResponse<Void>> build(
        HttpStatus status,
        String code,
        String message
    ) {

        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.<Void>builder()
                    .success(false)
                    .timestamp(Instant.now())
                    .errors(
                        List.of(
                            ApiError.builder()
                                .code(code)
                                .message(
                                    message != null
                                        ? message
                                        : status.getReasonPhrase()
                                )
                                .build()
                        )
                    )
                    .build()
            );
    }
}
