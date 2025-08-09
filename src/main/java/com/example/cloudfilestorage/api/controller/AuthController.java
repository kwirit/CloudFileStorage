package com.example.cloudfilestorage.api.controller;


import com.example.cloudfilestorage.api.dto.SignInRequest;
import com.example.cloudfilestorage.api.dto.UserResponse;
import com.example.cloudfilestorage.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUpController(@RequestBody SignInRequest requestData, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                requestData.getUsername(),
                requestData.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok(new UserResponse(requestData.getUsername()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signInController(@Valid @RequestBody SignInRequest requestData) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                requestData.getUsername(),
                requestData.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok(new UserResponse(requestData.getUsername()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOutController(HttpServletRequest request) {
        try {
            userService.signOut(request);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
