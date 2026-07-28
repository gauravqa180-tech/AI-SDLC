package com.ai.sdlc.expensetracker.budget.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record BudgetUpsertRequest(
        @NotNull @DecimalMin(value = "0.00", inclusive = true, message = "overallAmount must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "overallAmount must have up to 2 decimal places")
        BigDecimal overallAmount,

        @Valid
        @Size(max = 200, message = "too many category budgets")
        List<CategoryBudgetRequest> categoryBudgets
) {}
