package com.flowforge.backend.auth.security;

import com.flowforge.backend.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;

    public JwtService(
        @Value("${flowforge.security.jwt.secret}") String secret,
        @Value("${flowforge.security.jwt.access-token-expiration}")
        long accessTokenExpiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(
                    now.plusMillis(accessTokenExpiration)
                )
            )
            .signWith(signingKey)
            .compact();
    }

    public UUID extractUserId(String token) {

        Claims claims = parseClaims(token);

        return UUID.fromString(claims.getSubject());
    }

    public boolean isValid(String token) {

        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
