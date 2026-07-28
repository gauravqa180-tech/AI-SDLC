package com.aisdlc.expensetracker.api.dto;

import com.aisdlc.expensetracker.domain.ExpenseCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate expenseDate,
        @NotNull ExpenseCategory category,
        @Size(max = 500) String note
) {
}
