package com.example.test.security;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository accounts,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthRequests.TokenResponse register(AuthRequests.Registration registration) {
        String username = normalize(registration.username());
        if (accounts.existsByUsername(username)) {
            logger.warn("Account registration rejected: username already exists ({})", username);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
        }
        accounts.save(new User(
                username,
                passwordEncoder.encode(registration.password()),
                registration.email(),
                registration.mobileNumber()));
        logger.info("Account registered ({})", username);
        return tokenFor(username);
    }

    @Transactional
    public AuthRequests.TokenResponse login(AuthRequests.Credentials credentials) {
        String username = normalize(credentials.username());
        User account = accounts.findByUsername(username).orElse(null);
        if (account == null || !passwordEncoder.matches(credentials.password(), account.getPasswordHash())) {
            logger.warn("Login rejected: invalid credentials for username ({})", username);
            throw invalidCredentials();
        }
        account.recordLogin(LocalDateTime.now());
        logger.info("Login successful ({})", username);
        return tokenFor(username);
    }

    private AuthRequests.TokenResponse tokenFor(String username) {
        return new AuthRequests.TokenResponse(
                jwtService.createToken(username), "Bearer", jwtService.expirationSeconds());
    }

    private static String normalize(String username) {
        return username.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
