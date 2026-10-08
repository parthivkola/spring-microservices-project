package com.microservices.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/*
 * GATEWAY JWT FILTER — Authentication at the edge
 * ================================================
 *
 * Pattern: Gateway authenticates; downstream services authorize.
 *
 * 1. Extract Authorization: Bearer <token>
 * 2. Validate signature + expiry with shared secret
 * 3. Propagate identity as headers: X-User-Id, X-User-Roles
 * 4. Downstream services trust these headers (internal network) or re-validate JWT
 *
 * Public paths (no JWT): /api/auth/**, swagger, actuator health
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final SecretKey key;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing/invalid Authorization header for path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            String rolesCsv = roles == null ? "" : roles.stream().map(Object::toString).collect(Collectors.joining(","));

            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Roles", rolesCsv)
                    .header("X-User-Email", String.valueOf(claims.get("email")))
                    .build();

            log.debug("JWT OK userId={} roles={} path={}", claims.getSubject(), rolesCsv, path);
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            log.warn("JWT validation failed for path={}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico");
    }

    @Override
    public int getOrder() {
        // After logging (HIGHEST_PRECEDENCE) but before routing
        return -100;
    }
}
