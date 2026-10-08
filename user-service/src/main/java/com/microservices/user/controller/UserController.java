package com.microservices.user.controller;

import com.microservices.user.dto.UserResponse;
import com.microservices.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * UserController — GET /api/users/{id} used by order-service via Feign.
 * Password is NEVER returned (UserResponse strips it).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User lookup")
public class UserController {

    private final UserQueryService userQueryService;

    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id (used by order-service Feign client)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return userQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
