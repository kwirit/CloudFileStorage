package com.example.cloudfilestorage.api.controller;


import com.example.cloudfilestorage.api.dto.SignInUpRequest;
import com.example.cloudfilestorage.api.dto.UserResponse;
import com.example.cloudfilestorage.core.model.User;
import com.example.cloudfilestorage.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUpController(@Valid @RequestBody SignInUpRequest requestData) {
        User newUser = userService.signUp(requestData.getUsername(), requestData.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponse(requestData.getUsername()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signInController(@Valid @RequestBody SignInUpRequest requestData,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                requestData.getUsername(),
                requestData.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        return ResponseEntity.ok(new UserResponse(requestData.getUsername()));
    } // TODO: добавить кастомную валидацию пароля и логина

    @PostMapping("/sign-out")
    public void signOutController() {}

    @GetMapping("/pupupu-pupu")
    public ResponseEntity<?> bratsummer(HttpServletRequest request) {
        return ResponseEntity.ok(new UserResponse(request.getParameter("username")));
    }
}
