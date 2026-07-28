package com.example.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    BigDecimal amount,
    LocalDate date,
    String category,
    String note
) {}
