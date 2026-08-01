package com.example.expensetracker.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotBlank(message = "month is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format")
        String month,

        @Size(max = 64, message = "category must be at most 64 characters")
        String category,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "amount must have at most 2 decimal places")
        BigDecimal amount
) {
}
