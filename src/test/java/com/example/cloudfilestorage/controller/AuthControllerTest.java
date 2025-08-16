package com.example.cloudfilestorage.controller;


import com.example.cloudfilestorage.api.controller.AuthController;
import com.example.cloudfilestorage.api.dto.SignInUpRequest;
import com.example.cloudfilestorage.core.exception.AuthException.UserAlreadyExistsException;
import com.example.cloudfilestorage.core.exception.AuthException.UserNotFoundException;
import com.example.cloudfilestorage.config.SecurityConfig;
import com.example.cloudfilestorage.core.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "testuser", password = "password123")
    public void whenValidLogin_thenReturns201Created() throws Exception {
        SignInUpRequest request = new SignInUpRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    public void whenInvalidPassword_thenReturns400BadRequest() throws Exception {
        SignInUpRequest request = new SignInUpRequest();
        request.setUsername("wronguser");
        request.setPassword("wrng");

        when(userService.loadUserByUsername(request.getUsername()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenInvalidPassword_thenReturns401Unauthorized() throws Exception {
        SignInUpRequest request = new SignInUpRequest();
        request.setUsername("testuser");
        request.setPassword("qwerty");

        when(userService.loadUserByUsername(request.getUsername()))
                .thenReturn(
                        new User(
                                request.getUsername(),
                                new BCryptPasswordEncoder().encode(request.getPassword() + "1"), // Используем неправильный хешированный пароль
                                Collections.emptyList())
                );


        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    } // TODO: разобраться что не так

    @Test
    public void whenUserAlreadyExist_thenReturns409Conflict() throws Exception {
        SignInUpRequest request = new SignInUpRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userService.signUp(anyString(), anyString()))
                .thenThrow(new UserAlreadyExistsException("Данное имя пользователя уже занято"));

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
