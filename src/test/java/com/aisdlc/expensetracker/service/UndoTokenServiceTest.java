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

        String token = service.issueToken(1L, Duration.ofSeconds(1));

        // Move time forward by recreating service with later clock but same token store isn't shared.
        // So validate expiration logic by consuming after issuing with a clock that is already after expiry.
        Clock after = Clock.fixed(Instant.parse("2024-01-01T00:00:02Z"), ZoneOffset.UTC);
        UndoTokenService serviceAfter = new UndoTokenService(after);
        // token does not exist in this service; so instead assert that consuming in original service after expiry yields empty is not possible.
        // Keep this test focused on internal expiry check by inserting directly through public API.
        // Workaround: issue with negative ttl.
        String expiredToken = service.issueToken(2L, Duration.ofSeconds(-1));
        assertThat(service.consume(expiredToken)).isEmpty();
    }
}
