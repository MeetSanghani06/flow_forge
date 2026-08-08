package com.flowforge.backend.auth.security;

import com.flowforge.backend.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
    extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String header =
            request.getHeader("Authorization");

        if (header == null
            || !header.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
            header.substring(7);

        if (!jwtService.isValid(token)) {

            filterChain.doFilter(request, response);
            return;
        }

        UUID userId =
            jwtService.extractUserId(token);

        userRepository.findById(userId)
            .filter(user -> user.isEnabled())
            .ifPresent(user -> {

                var authorities = List.of(
                    new SimpleGrantedAuthority(
                        "ROLE_" +
                            user.getRole().name()
                    )
                );

                var authentication =
                    new UsernamePasswordAuthenticationToken(
                        user.getId(),
                        null,
                        authorities
                    );

                SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                        authentication
                    );
            });

        filterChain.doFilter(request, response);
    }
}
