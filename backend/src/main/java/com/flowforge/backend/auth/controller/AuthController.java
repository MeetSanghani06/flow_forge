package com.flowforge.backend.auth.controller;

import com.flowforge.backend.auth.dto.*;
import com.flowforge.backend.auth.service.AuthService;
import com.flowforge.backend.auth.service.UserService;
import com.flowforge.backend.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {

        return ApiResponse.success(
            userService.register(request)
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {

        return ApiResponse.success(
            authService.login(request)
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
        @Valid @RequestBody RefreshTokenRequest request
    ) {

        return ApiResponse.success(
            authService.refresh(request)
        );
    }
}
