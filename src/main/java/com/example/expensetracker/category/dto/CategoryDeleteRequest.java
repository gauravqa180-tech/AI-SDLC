package com.example.expensetracker.category.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryDeleteRequest(
        @NotNull Long reassignToCategoryId
) {
}
