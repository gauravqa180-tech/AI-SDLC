package com.example.expensetracker.expenses.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpsertRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull LocalDate date,
        @NotNull Long categoryId,
        @Size(max = 500) String note
) {
}
