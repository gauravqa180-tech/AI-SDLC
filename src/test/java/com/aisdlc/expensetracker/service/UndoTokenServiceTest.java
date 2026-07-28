package com.aisdlc.expensetracker.service;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class UndoTokenServiceTest {

    @Test
    void shouldConsumeValidTokenOnce() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        UndoTokenService service = new UndoTokenService(clock);

        String token = service.issueToken(1L, Duration.ofSeconds(10));

        assertThat(service.consume(token)).isPresent();
        assertThat(service.consume(token)).isEmpty();
    }

    @Test
    void shouldNotConsumeExpiredToken() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        UndoTokenService service = new UndoTokenService(clock);

        // Issue an already-expired token by using a negative TTL.
        String expiredToken = service.issueToken(2L, Duration.ofSeconds(-1));

        assertThat(service.consume(expiredToken)).isEmpty();
    }
}
