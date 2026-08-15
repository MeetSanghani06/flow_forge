package com.flowforge.backend.auth.dto;

public record AuthResponse(

    String accessToken,

    String refreshToken,

    String tokenType,

    long expiresIn

) {
}
