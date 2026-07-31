package com.example.expensetracker.api.dto;

import com.example.expensetracker.domain.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpsertBudgetRequest(
        @NotNull(message = "month is required")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be yyyy-MM")
        String month,

        @NotNull(message = "category is required")
        ExpenseCategory category,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "amount must be > 0")
        BigDecimal amount
) {
}
