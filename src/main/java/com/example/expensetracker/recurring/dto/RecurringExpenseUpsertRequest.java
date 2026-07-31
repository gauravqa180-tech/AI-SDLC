package com.example.expensetracker.recurring.dto;

import com.example.expensetracker.recurring.RecurringCadence;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseUpsertRequest(
        @NotNull @DecimalMin(value = "0.01", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull Long categoryId,
        @Size(max = 500) String note,
        @NotNull RecurringCadence cadence,
        @NotNull LocalDate startDate,
        @NotNull Boolean enabled
) {
}
