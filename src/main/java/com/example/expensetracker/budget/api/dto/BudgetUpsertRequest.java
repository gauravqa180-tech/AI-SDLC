package com.example.expensetracker.budget.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetUpsertRequest(
        @NotNull Integer year,
        @NotNull Integer month,
        @NotBlank String category,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount
) {
}
