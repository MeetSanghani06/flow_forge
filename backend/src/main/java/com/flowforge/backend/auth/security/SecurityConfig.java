package com.flowforge.backend.auth.security;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.flowforge.backend.common.response.ApiError;
import com.flowforge.backend.common.response.ApiResponse;
import com.flowforge.backend.common.enums.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .exceptionHandling(exception ->
                exception
                    .authenticationEntryPoint(
                        authenticationEntryPoint(objectMapper)
                    )
                    .accessDeniedHandler(
                        accessDeniedHandler(objectMapper)
                    )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/system/health",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest()
                .authenticated()
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
        ObjectMapper objectMapper
    ) {

        return (request, response, exception) -> {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
            );

            ApiResponse<Void> body =
                ApiResponse.<Void>builder()
                    .success(false)
                    .timestamp(Instant.now())
                    .errors(
                        List.of(
                            ApiError.builder()
                                .code(ErrorCode.UNAUTHORIZED.name())
                                .message("Authentication required")
                                .build()
                        )
                    )
                    .build();

            objectMapper.writeValue(
                response.getOutputStream(),
                body
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(
        ObjectMapper objectMapper
    ) {

        return (request, response, exception) -> {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
            );

            ApiResponse<Void> body =
                ApiResponse.<Void>builder()
                    .success(false)
                    .timestamp(Instant.now())
                    .errors(
                        List.of(
                            ApiError.builder()
                                .code("FORBIDDEN")
                                .message("Access denied")
                                .build()
                        )
                    )
                    .build();

            objectMapper.writeValue(
                response.getOutputStream(),
                body
            );
        };
    }
}
