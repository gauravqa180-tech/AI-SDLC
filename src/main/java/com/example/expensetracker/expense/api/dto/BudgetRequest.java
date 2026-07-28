package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetRequest(
        @NotNull(message = "month is required") YearMonth month,
        String category,
        @NotNull @DecimalMin(value = "0.01", message = "amount must be greater than 0") BigDecimal amount,
        @DecimalMin(value = "0.0", message = "warnThresholdPct must be between 0 and 1") BigDecimal warnThresholdPct
) {
}
