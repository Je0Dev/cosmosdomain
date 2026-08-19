package com.cosmosdomain.config;

import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DataSeeder {

    @Bean
    CommandLineRunner initTestData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (!userRepository.existsByUsername("testuser")) {
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setFirstName("Test");
                user.setLastName("User");
                user.setPasswordHash(passwordEncoder.encode("test123"));
                user.setActive(true);
                user.setLocked(false);
                userRepository.save(user);
                System.out.println("Created test user: testuser / test123");
            }

            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setActive(true);
                admin.setLocked(false);
                userRepository.save(admin);
                System.out.println("Created admin user: admin / admin123");
            }
        };
    }
}