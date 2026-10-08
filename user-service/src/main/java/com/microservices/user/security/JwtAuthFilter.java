package com.microservices.user.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * JwtAuthFilter — extracts Bearer token, validates, populates SecurityContext.
 *
 * Runs once per request (OncePerRequestFilter). If the gateway already validated
 * the JWT and forwarded X-User-Id / X-User-Roles, we also accept those headers
 * so internal Feign calls without a full JWT still work behind the gateway.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtService.parseClaims(token);
                setAuthentication(claims.getSubject(), claims.get("roles", List.class));
            } catch (Exception ignored) {
                // Invalid token → leave context empty; SecurityConfig will return 401
            }
        } else {
            // Trust gateway-propagated identity headers for internal traffic
            String userId = request.getHeader("X-User-Id");
            String rolesHeader = request.getHeader("X-User-Roles");
            if (userId != null && rolesHeader != null) {
                List<String> roles = List.of(rolesHeader.split(","));
                setAuthentication(userId, roles);
            }
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private void setAuthentication(String userId, Collection<?> roles) {
        List<SimpleGrantedAuthority> authorities = roles == null
                ? List.of()
                : roles.stream()
                .map(Object::toString)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
