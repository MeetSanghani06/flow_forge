package com.flowforge.backend.auth.service;

import com.flowforge.backend.auth.dto.AuthResponse;
import com.flowforge.backend.auth.dto.LoginRequest;
import com.flowforge.backend.auth.dto.RefreshTokenRequest;
import com.flowforge.backend.auth.entity.RefreshToken;
import com.flowforge.backend.auth.entity.User;
import com.flowforge.backend.auth.repository.RefreshTokenRepository;
import com.flowforge.backend.auth.repository.UserRepository;
import com.flowforge.backend.auth.security.JwtService;
import com.flowforge.backend.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${flowforge.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
            .findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() ->
                new UnauthorizedException(
                    "Invalid email or password"
                )
            );

        if (!user.isEnabled()
            || !passwordEncoder.matches(
            request.password(),
            user.getPasswordHash()
        )) {
            throw new UnauthorizedException(
                "Invalid email or password"
            );
        }

        String accessToken =
            jwtService.generateAccessToken(user);

        String refreshToken = generateRefreshToken();

        saveRefreshToken(user, refreshToken);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            900
        );
    }

    private void saveRefreshToken(
        User user,
        String rawToken
    ) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(
            Instant.now().plusMillis(refreshTokenExpiration)
        );
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    private String generateRefreshToken() {

        byte[] bytes = new byte[64];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
                )
            );

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                "Unable to hash refresh token",
                ex
            );
        }
    }

    @Transactional
    public AuthResponse refresh(
        RefreshTokenRequest request
    ) {

        String tokenHash =
            hashToken(request.refreshToken());

        RefreshToken refreshToken =
            refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                    new UnauthorizedException(
                        "Invalid refresh token"
                    )
                );

        if (refreshToken.isRevoked()
            || refreshToken.getExpiresAt()
            .isBefore(Instant.now())) {

            throw new UnauthorizedException(
                "Refresh token expired or revoked"
            );
        }

        User user = refreshToken.getUser();

        refreshToken.setRevoked(true);

        String newAccessToken =
            jwtService.generateAccessToken(user);

        String newRefreshToken =
            generateRefreshToken();

        saveRefreshToken(
            user,
            newRefreshToken
        );

        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            900
        );
    }
}
