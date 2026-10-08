package com.microservices.user.dto;

import com.microservices.user.model.User;

/*
 * Public user view — never exposes the password hash.
 */
public record UserResponse(String id, String name, String email, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
