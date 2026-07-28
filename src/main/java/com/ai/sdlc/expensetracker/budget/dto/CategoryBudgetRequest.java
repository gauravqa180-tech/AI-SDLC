package com.ai.sdlc.expensetracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CategoryBudgetRequest(
        @NotNull Long categoryId,
        @NotNull @DecimalMin(value = "0.00", inclusive = true, message = "amount must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "amount must have up to 2 decimal places")
        BigDecimal amount
) {}
