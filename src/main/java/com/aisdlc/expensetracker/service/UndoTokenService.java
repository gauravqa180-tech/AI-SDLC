package com.aisdlc.expensetracker.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UndoTokenService {

    public record UndoInfo(Long expenseId, Instant expiresAt) {
    }

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, UndoInfo> tokenStore = new ConcurrentHashMap<>();
    private final Clock clock;

    public UndoTokenService() {
        this(Clock.systemUTC());
    }

    // for tests
    UndoTokenService(Clock clock) {
        this.clock = clock;
    }

    public String issueToken(Long expenseId, Duration ttl) {
        String token = generateToken();
        tokenStore.put(token, new UndoInfo(expenseId, Instant.now(clock).plus(ttl)));
        return token;
    }

    public Optional<UndoInfo> consume(String token) {
        UndoInfo info = tokenStore.remove(token);
        if (info == null) {
            return Optional.empty();
        }
        if (Instant.now(clock).isAfter(info.expiresAt())) {
            return Optional.empty();
        }
        return Optional.of(info);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
