package com.microservices.user.dto;

/*
 * Returned after login/register.
 * Client stores `token` and sends it as: Authorization: Bearer <token>
 */
public record AuthResponse(
        String token,
        String tokenType,
        String userId,
        String email,
        String role
) {
    public static AuthResponse of(String token, String userId, String email, String role) {
        return new AuthResponse(token, "Bearer", userId, email, role);
    }
}
