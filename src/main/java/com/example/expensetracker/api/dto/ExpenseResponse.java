package com.example.expensetracker.api.dto;

import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        ExpenseCategory category,
        String note
) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
