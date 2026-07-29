package com.example.expensetracker.category.api.dto;

import jakarta.validation.constraints.NotNull;

public record CategoryMergeRequest(
        @NotNull(message = "sourceCategoryId is required")
        Long sourceCategoryId,

        @NotNull(message = "targetCategoryId is required")
        Long targetCategoryId
) {
}
