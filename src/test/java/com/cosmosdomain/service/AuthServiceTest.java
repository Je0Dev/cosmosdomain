package com.cosmosdomain.service;

import com.cosmosdomain.entity.User;
import com.cosmosdomain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}hashed");
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void testRegister_NewUser_SavesWithHashedPassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register("newuser", "new@test.com",
            "password123", "John", "Doe");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(1L, result.getId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertNotEquals("password123", captured.getPasswordHash());
        assertTrue(captured.getPasswordHash().startsWith("{bcrypt}"));
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authService.register("existing", "new@test.com",
                "password123", "John", "Doe"));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authService.register("newuser", "existing@test.com",
                "password123", "John", "Doe"));
        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}