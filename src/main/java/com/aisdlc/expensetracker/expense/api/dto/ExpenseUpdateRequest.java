package com.aisdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 11, fraction = 2) BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 255) String note
) {
}
