package com.ai.sdlc.expensetracker.expense.api.dto;

import java.time.Instant;

public record DeleteResponse(
        Long id,
        boolean deleted,
        Instant deletedAt,
        Instant undoUntil
) {
}
