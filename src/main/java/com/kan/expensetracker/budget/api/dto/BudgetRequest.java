package com.kan.expensetracker.budget.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetRequest(
        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Year must be >= 2000")
        Integer year,

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        // null => overall
        Long categoryId,

        @NotNull(message = "Budget amount is required")
        @DecimalMin(value = "0.01", message = "Budget amount must be > 0")
        @Digits(integer = 17, fraction = 2, message = "Budget amount must have up to 2 decimal places")
        BigDecimal amount,

        // optional thresholds. If null, defaults will be used.
        @DecimalMin(value = "0.01", message = "warnThreshold must be > 0")
        @DecimalMax(value = "1.00", message = "warnThreshold must be <= 1")
        BigDecimal warnThreshold,

        @DecimalMin(value = "0.01", message = "exceedThreshold must be > 0")
        @DecimalMax(value = "5.00", message = "exceedThreshold must be <= 5")
        BigDecimal exceedThreshold
) {
}
