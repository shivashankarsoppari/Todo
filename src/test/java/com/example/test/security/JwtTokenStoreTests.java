package com.example.test.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class JwtTokenStoreTests {

    @Test
    void storesOnlyTokenFingerprintWithExpiryAndChecksUsername() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(values);

        JwtTokenStore tokenStore = new JwtTokenStore(redisTemplate, "test:jwt:");
        String token = "sensitive-bearer-token";
        Duration ttl = Duration.ofMinutes(15);

        tokenStore.store(token, "test-user", ttl);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(values).set(keyCaptor.capture(), eq("test-user"), eq(ttl));
        String key = keyCaptor.getValue();
        assertThat(key).startsWith("test:jwt:");
        assertThat(key).doesNotContain(token);
        assertThat(key).hasSize("test:jwt:".length() + 64);

        when(values.get(key)).thenReturn("test-user");
        assertThat(tokenStore.contains(token, "test-user")).isTrue();
        assertThat(tokenStore.contains(token, "another-user")).isFalse();
    }
}
