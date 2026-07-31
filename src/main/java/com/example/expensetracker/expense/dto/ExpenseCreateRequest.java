package com.example.expensetracker.expense.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @NotNull Long categoryId,
        @Size(max = 255) String note
) {
}
