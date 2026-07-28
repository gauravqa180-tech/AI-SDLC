package com.aisdlc.expensetracker.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 64, message = "Category name must be at most 64 characters")
        String name
) {
}
