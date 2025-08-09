package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.core.exception.AuthException.UserAlreadyExistsException;
import com.example.cloudfilestorage.core.exception.AuthException.UserNotFoundException;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.UserRepository;
import com.example.cloudfilestorage.core.validation.PasswordValidator;
import com.example.cloudfilestorage.core.validation.UsernameValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final PasswordValidator passwordValidator;
    private final UsernameValidator usernameValidator;
    private final UserRepository userRepository;

    public UserService(PasswordValidator passwordValidator,
                       UsernameValidator usernameValidator,
                       UserRepository userRepository) {
        this.passwordValidator = passwordValidator;
        this.usernameValidator = usernameValidator;
        this.userRepository = userRepository;
    }

    public String signUp(String username, String password) {
        return username;
    }
    public String signOut(HttpServletRequest request) throws Exception {
        return "Sign Out Successfully";
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("Пользователь с именем пользователя: + %s + не найден", username)
                ));


        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

}
