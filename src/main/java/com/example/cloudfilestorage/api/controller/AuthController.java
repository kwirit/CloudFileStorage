package com.example.cloudfilestorage.api.controller;


import com.example.cloudfilestorage.core.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {this.userService = userService;}

    @PostMapping("/sign-up")
    public String SignUpController(@RequestBody String username, @RequestBody String password) {
        return username;
    }

    @PostMapping("/sign-in")
    public String SignInController(@RequestBody String username, @RequestBody String password) {
        userService.signIn();
        return username;
    }
}
