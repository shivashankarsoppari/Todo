package com.example.test.security;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    public record Registration(
            @NotBlank
            @Size(min = 3, max = 50)
            @Pattern(regexp = "[A-Za-z0-9._-]+")
            String username,
            @NotBlank
            @Size(min = 12, max = 72)
            String password,
            @Email
            @Size(max = 254)
            String email,
            @Pattern(regexp = "\\+?[0-9]{7,15}", message = "mobileNumber must contain 7 to 15 digits, optionally prefixed by +")
            String mobileNumber) {

        public Registration {
            email = normalizeOptional(email);
            mobileNumber = normalizeOptional(mobileNumber);
        }

        @AssertTrue(message = "password must not exceed 72 UTF-8 bytes")
        public boolean isPasswordWithinBcryptLimit() {
            return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
    }

    public record TokenResponse(String token, String tokenType, long expiresIn) {
    }

    public record UserProfile(String username) {
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
