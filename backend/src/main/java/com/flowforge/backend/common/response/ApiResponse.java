package com.flowforge.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;

    private final Instant timestamp;

    private final T data;

    private final Object meta;

    private final Object errors;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .timestamp(Instant.now())
            .data(data)
            .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
            .success(true)
            .timestamp(Instant.now())
            .build();
    }
}
