package com.ai.sdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01", message = "amount must be > 0") BigDecimal amount,
        @NotNull LocalDate date,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 1000) String note
) {
}
