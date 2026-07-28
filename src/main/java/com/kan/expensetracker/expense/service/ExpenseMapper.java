package com.kan.expensetracker.expense.service;

import com.kan.expensetracker.expense.api.dto.ExpenseResponse;
import com.kan.expensetracker.expense.domain.Expense;

public final class ExpenseMapper {
    private ExpenseMapper() {}

    public static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getNote(),
                e.isDeleted()
        );
    }
}
