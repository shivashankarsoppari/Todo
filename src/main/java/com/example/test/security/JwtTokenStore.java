package com.example.test.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;

    public JwtTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.jwt.token-key-prefix:todo-app:jwt:}") String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }

    public void store(String token, String username, Duration ttl) {
        redisTemplate.opsForValue().set(key(token), username, ttl);
    }

    public boolean contains(String token, String username) {
        return username.equals(redisTemplate.opsForValue().get(key(token)));
    }

    public void delete(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        try {
            byte[] fingerprint = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return keyPrefix + HexFormat.of().formatHex(fingerprint);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
