package com.cosmosdomain.service;

import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Authentication and registration service.
 *
 * <p>Implements Spring Security's {@link UserDetailsService} so the framework
 * can load users during form login, and exposes the registration logic used by
 * {@link com.cosmosdomain.controller.AuthController}.</p>
 */
@Service
@Transactional
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Called by Spring Security during form login.
     *
     * <p>The constructor arguments map to: username, password hash, enabled,
     * account non-expired, credentials non-expired, account non-locked, and
     * authorities. This app uses authentication only, so the authority list is
     * intentionally empty.</p>
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            user.isActive(),
            true, true, !user.isLocked(),
            new ArrayList<>()
        );
    }

    /**
     * Creates a new active, unlocked account after checking uniqueness.
     *
     * @throws IllegalArgumentException when the username or email is taken
     */
    public User register(String username, String email, String password,
                         String firstName, String lastName) {
        // Uniqueness is checked up front to produce a friendly form error; the
        // database unique constraints remain the final safeguard.
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email.toLowerCase());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // Passwords are never stored in plain text.
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);
        user.setLocked(false);

        return userRepository.save(user);
    }
}