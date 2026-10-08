package com.microservices.user.config;

import com.microservices.user.model.Role;
import com.microservices.user.model.User;
import com.microservices.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * Seeds demo users so Feign + JWT flows work immediately.
 *
 *   u1 / alice@example.com / password  → ROLE_USER
 *   u2 / bob@example.com   / password  → ROLE_USER
 *   admin / admin@example.com / admin123 → ROLE_ADMIN
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new User("u1", "Alice Smith", "alice@example.com",
                passwordEncoder.encode("password"), Role.USER));
        userRepository.save(new User("u2", "Bob Johnson", "bob@example.com",
                passwordEncoder.encode("password"), Role.USER));
        userRepository.save(new User("admin", "Admin User", "admin@example.com",
                passwordEncoder.encode("admin123"), Role.ADMIN));
        log.info("Seeded demo users: alice@example.com / password, admin@example.com / admin123");
    }
}
