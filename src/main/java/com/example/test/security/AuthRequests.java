package com.example.test.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import java.nio.charset.StandardCharsets;

public final class AuthRequests {

    private AuthRequests() {
    }

    public record Credentials(
            @NotBlank
            @Size(min = 3, max = 50)
            @Pattern(regexp = "[A-Za-z0-9._-]+")
            String username,
            @NotBlank
            @Size(min = 12, max = 72)
            String password) {

        @AssertTrue(message = "password must not exceed 72 UTF-8 bytes")
        public boolean isPasswordWithinBcryptLimit() {
            return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }

    public record TokenResponse(String token, String tokenType, long expiresIn) {
    }
}
