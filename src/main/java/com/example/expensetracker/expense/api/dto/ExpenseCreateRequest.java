package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true, message = "amount must be greater than 0") BigDecimal amount,
        @NotNull(message = "date is required") LocalDate date,
        @NotBlank(message = "category is required") String category,
        String note
) {
}
