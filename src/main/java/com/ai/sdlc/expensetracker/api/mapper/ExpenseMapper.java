package com.ai.sdlc.expensetracker.api.mapper;

import com.ai.sdlc.expensetracker.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.domain.Expense;

public final class ExpenseMapper {

    private ExpenseMapper() {
    }

    public static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory(),
                e.getNote(),
                e.getDeletedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
