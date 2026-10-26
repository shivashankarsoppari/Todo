package com.example.test.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String encodedSecret,
            @Value("${app.jwt.expiration:PT15M}") Duration expiration) {
        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalStateException("app.jwt.expiration must be positive");
        }
        byte[] keyBytes = signingKeyBytes(encodedSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    private static byte[] signingKeyBytes(String encodedSecret) {
        if (encodedSecret.isBlank()) {
            byte[] ephemeralKey = new byte[32];
            new SecureRandom().nextBytes(ephemeralKey);
            logger.warn("JWT_SECRET is unset; using a temporary key. Tokens will not survive restart. "
                    + "Configure JWT_SECRET for persistent sessions and production.");
            return ephemeralKey;
        }

        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET is not valid base64. Set it to base64 for at least 32 cryptographically random bytes.",
                    exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET decodes to fewer than 32 bytes. Generate at least 32 random bytes and base64-encode them.");
        }
        return keyBytes;
    }

    public String createToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey)
                .compact();
    }

    public String usernameFrom(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public long expirationSeconds() {
        return expiration.toSeconds();
    }
}
