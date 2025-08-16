package com.example.cloudfilestorage.core.service;

import com.example.cloudfilestorage.core.exception.AuthException.UserAlreadyExistsException;
import com.example.cloudfilestorage.core.exception.AuthException.UserNotFoundException;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signUp(String username, String password) throws UserAlreadyExistsException {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(
                    String.format("Пользователь с именем пользователя: + %s + уже существует", username)
            );
        }
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(username, encodedPassword);

        return userRepository.save(user);
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
