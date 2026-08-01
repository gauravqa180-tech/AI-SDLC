package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.ExpenseRequest;
import com.example.expensetracker.api.dto.ExpenseResponse;
import com.example.expensetracker.domain.Expense;

import java.time.OffsetDateTime;

final class ExpenseMapper {
    private ExpenseMapper() {
    }

    static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory(),
                e.getNote(),
                e.getVersion(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    static void apply(ExpenseRequest req, Expense e) {
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        e.setUpdatedAt(OffsetDateTime.now());
    }
}
