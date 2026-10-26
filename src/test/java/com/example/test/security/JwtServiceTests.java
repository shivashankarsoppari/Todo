package com.example.test.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    @Test
    void generatesTemporarySigningSecretWhenConfigurationIsMissing() {
        JwtService service = assertDoesNotThrow(() -> new JwtService("", Duration.ofMinutes(15)));
        assertTrue(service.usernameFrom(service.createToken("test-user")).equals("test-user"));
    }

    @Test
    void explainsWhenSigningSecretIsNotBase64() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> new JwtService("not-base64!", Duration.ofMinutes(15)));

        assertTrue(exception.getMessage().contains("not valid base64"));
    }

    @Test
    void explainsWhenSigningSecretIsTooShort() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> new JwtService("c2hvcnQ=", Duration.ofMinutes(15)));

        assertTrue(exception.getMessage().contains("fewer than 32 bytes"));
    }
}
