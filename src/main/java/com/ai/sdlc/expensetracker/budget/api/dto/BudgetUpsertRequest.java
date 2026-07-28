package com.ai.sdlc.expensetracker.budget.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotBlank(message = "month is required")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in yyyy-MM format")
        String month,

        @NotBlank(message = "category is required")
        @Size(max = 64, message = "category must be at most 64 characters")
        String category,

        @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "budget amount must be greater than 0")
        BigDecimal amount
) {
}
