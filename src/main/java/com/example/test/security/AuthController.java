package com.example.test.security;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRequests.TokenResponse register(@Valid @RequestBody AuthRequests.Credentials credentials) {
        return authService.register(credentials);
    }

    @PostMapping("/login")
    public AuthRequests.TokenResponse login(@Valid @RequestBody AuthRequests.Credentials credentials) {
        return authService.login(credentials);
    }
}
