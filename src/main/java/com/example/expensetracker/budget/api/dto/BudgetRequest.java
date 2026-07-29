package com.example.expensetracker.budget.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(
        /**
         * Format: YYYY-MM
         */
        @NotBlank(message = "month is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format")
        String month,

        /**
         * Null/blank means overall monthly budget.
         */
        @Size(max = 100, message = "category must be <= 100 characters")
        String category,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "amount must be >= 0")
        @Digits(integer = 17, fraction = 2, message = "amount must be a valid currency amount")
        BigDecimal amount
) {
}
