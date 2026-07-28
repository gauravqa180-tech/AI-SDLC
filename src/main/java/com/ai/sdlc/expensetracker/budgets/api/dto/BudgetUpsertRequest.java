package com.ai.sdlc.expensetracker.budgets.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record BudgetUpsertRequest(
        @NotNull(message = "overallAmount is required")
        @DecimalMin(value = "0.01", message = "overallAmount must be greater than 0")
        @Digits(integer = 17, fraction = 2)
        BigDecimal overallAmount,

        List<CategoryBudgetRequest> categoryBudgets
) {

    public record CategoryBudgetRequest(
            @NotBlank(message = "category is required")
            @Size(max = 100)
            String category,

            @NotNull(message = "amount is required")
            @DecimalMin(value = "0.01", message = "amount must be greater than 0")
            @Digits(integer = 17, fraction = 2)
            BigDecimal amount
    ) {
    }
}
