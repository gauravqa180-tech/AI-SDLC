package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.ExpenseResponse;
import com.example.expensetracker.domain.Expense;

public final class ExpenseMapper {
    private ExpenseMapper() {
    }

    public static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
