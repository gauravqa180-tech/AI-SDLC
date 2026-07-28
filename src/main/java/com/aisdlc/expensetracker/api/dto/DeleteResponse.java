package com.aisdlc.expensetracker.api.dto;

import java.time.OffsetDateTime;

public record DeleteResponse(
        Long expenseId,
        boolean deleted,
        OffsetDateTime deletedAt,
        String undoToken
) {
}
