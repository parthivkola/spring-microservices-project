package com.microservices.user.security;

import com.microservices.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/*
 * JwtService — issues and validates JWTs (HS256).
 *
 * JWT STRUCTURE (three Base64URL parts separated by '.'):
 *   header.payload.signature
 *   header  = { "alg": "HS256", "typ": "JWT" }
 *   payload = { "sub": userId, "email": ..., "roles": ["USER"], "iat": ..., "exp": ... }
 *   signature = HMAC-SHA256(header + "." + payload, secret)
 *
 * WHY HS256 HERE (vs RS256 in some designs)?
 *   Shared secret is simpler for a learning project: gateway + user-service
 *   both use the same JWT_SECRET. Production often uses RS256 so only the
 *   auth service holds the private key; others verify with a public key.
 *
 * MUST match the secret configured in api-gateway JwtAuthenticationFilter.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        // HS256 requires >= 256 bits (32 bytes)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId())
                .claim("email", user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
