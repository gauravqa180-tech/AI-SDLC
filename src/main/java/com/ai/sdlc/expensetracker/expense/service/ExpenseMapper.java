package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.domain.Expense;

public final class ExpenseMapper {
    private ExpenseMapper() {
    }

    public static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory(),
                e.getNote()
        );
    }
}
