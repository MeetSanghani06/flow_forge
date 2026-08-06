package com.flowforge.backend.common.response;

import lombok.Builder;

@Builder
public record ApiError(

    String code,

    String message,

    String field

) {
}
