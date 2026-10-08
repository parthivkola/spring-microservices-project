package com.microservices.user.service;

import com.microservices.user.dto.UserResponse;
import com.microservices.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryService {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserResponse> findById(String id) {
        return userRepository.findById(id).map(UserResponse::from);
    }
}
