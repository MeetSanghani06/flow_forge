package com.flowforge.backend.common.exception;

import com.flowforge.backend.common.enums.ErrorCode;
import com.flowforge.backend.common.response.ApiError;
import com.flowforge.backend.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ApiResponse.<Void>builder()
                    .success(false)
                    .timestamp(Instant.now())
                    .errors(
                        List.of(
                            ApiError.builder()
                                .code(ErrorCode.RESOURCE_NOT_FOUND.name())
                                .message(ex.getMessage())
                                .build()
                        )
                    )
                    .build()
            );

    }

}
