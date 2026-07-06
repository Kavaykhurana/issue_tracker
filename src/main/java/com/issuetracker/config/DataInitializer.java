package com.issuetracker.config;

import com.issuetracker.entity.User;
import com.issuetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        bootstrapUser("admin", "admin@example.com", "admin123", "Administrator");
        bootstrapUser("dev1",   "dev1@example.com",  "dev123",   "Developer One");
        bootstrapUser("dev2",   "dev2@example.com",  "dev223",   "Developer Two");
    }

    private void bootstrapUser(String username, String email, String password, String displayName) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .displayName(displayName)
                    .build();
            userRepository.save(user);
        }
    }
}
