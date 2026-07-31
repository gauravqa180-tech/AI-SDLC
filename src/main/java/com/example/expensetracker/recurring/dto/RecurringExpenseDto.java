package com.example.expensetracker.recurring.dto;

import com.example.expensetracker.recurring.RecurringCadence;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseDto(
        Long id,
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        String note,
        RecurringCadence cadence,
        LocalDate startDate,
        boolean enabled
) {
}
