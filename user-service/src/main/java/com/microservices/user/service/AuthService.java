package com.microservices.user.service;

import com.microservices.user.dto.AuthResponse;
import com.microservices.user.dto.LoginRequest;
import com.microservices.user.dto.RegisterRequest;
import com.microservices.user.model.Role;
import com.microservices.user.model.User;
import com.microservices.user.repository.UserRepository;
import com.microservices.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }
        User user = new User(
                UUID.randomUUID().toString(),
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );
        userRepository.save(user);
        return AuthResponse.of(jwtService.generateToken(user), user.getId(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return AuthResponse.of(jwtService.generateToken(user), user.getId(), user.getEmail(), user.getRole().name());
    }
}
