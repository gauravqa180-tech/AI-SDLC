package com.ai.sdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
    @NotNull LocalDate date,
    @NotBlank @Size(max = 100) String category,
    @Size(max = 500) String note
) {
}
