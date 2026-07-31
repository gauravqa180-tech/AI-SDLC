package com.kan.expensetracker.api.dto;

import com.kan.expensetracker.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        Category category,
        String note
) {
}
