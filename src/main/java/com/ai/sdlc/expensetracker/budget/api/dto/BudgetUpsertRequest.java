package com.ai.sdlc.expensetracker.budget.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record BudgetUpsertRequest(
        @NotNull YearMonth month,

        @DecimalMin(value = "0.00", inclusive = true, message = "monthlyBudget must be >= 0")
        BigDecimal monthlyBudget,

        @Valid
        List<CategoryBudgetRequest> categoryBudgets
) {
    public record CategoryBudgetRequest(
            @NotBlank @Size(max = 80) String category,
            @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "budgetAmount must be > 0") BigDecimal budgetAmount
    ) {
    }
}
