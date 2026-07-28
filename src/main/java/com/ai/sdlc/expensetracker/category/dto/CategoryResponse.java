package com.ai.sdlc.expensetracker.category.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
