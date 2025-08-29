package com.example.cloudfilestorage.service;

import com.example.cloudfilestorage.core.exception.AuthException.UserAlreadyExistsException;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.UserRepository;
import com.example.cloudfilestorage.core.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "encodedPassword");
    }

    @Test
    void whenValidUser_thenSignUpSuccess() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.signUp("testuser", "password123");

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenUserAlreadyExists_thenThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () ->
                userService.signUp("testuser", "password123")
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}