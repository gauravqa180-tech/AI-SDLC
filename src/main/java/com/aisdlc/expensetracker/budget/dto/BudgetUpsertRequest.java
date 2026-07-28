package com.aisdlc.expensetracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotNull(message = "Budget amount is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Budget amount must be greater than 0")
        @Digits(integer = 11, fraction = 2, message = "Budget amount must have up to 11 integer digits and 2 decimal places")
        BigDecimal amount
) {
}
