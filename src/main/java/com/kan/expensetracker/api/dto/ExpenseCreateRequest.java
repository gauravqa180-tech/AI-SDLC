package com.kan.expensetracker.api.dto;

import com.kan.expensetracker.domain.Category;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseCreateRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotNull LocalDate date,
        @NotNull Category category,
        @Size(max = 1024) String note
) {
}
