package com.microservices.user.controller;

import com.microservices.user.dto.AuthResponse;
import com.microservices.user.dto.LoginRequest;
import com.microservices.user.dto.RegisterRequest;
import com.microservices.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * Auth endpoints — public (no JWT required).
 * After login, client sends: Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login and registration — issues JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new USER account and receive a JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email/password and receive a JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
