package com.example.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 64) String category,
        @Size(max = 512) String note
) {
}
