package com.ai.sdlc.expensetracker.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be <= 100 characters")
        String name
) {}
