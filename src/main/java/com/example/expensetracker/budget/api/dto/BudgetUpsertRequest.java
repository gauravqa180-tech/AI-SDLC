package com.example.expensetracker.budget.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        /** month in YYYY-MM */
        @NotBlank @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "month must be in YYYY-MM format") String month,
        /** null/blank indicates overall budget */
        @Size(max = 64) String category,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {
}
