package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.domain.Expense;

final class ExpenseMapper {

    private ExpenseMapper() {
    }

    static Expense toEntity(ExpenseCreateRequest request) {
        Expense e = new Expense();
        e.setAmount(request.amount());
        e.setDate(request.date());
        e.setCategory(request.category().trim());
        e.setNote(request.note());
        return e;
    }

    static void apply(ExpenseUpdateRequest request, Expense e) {
        e.setAmount(request.amount());
        e.setDate(request.date());
        e.setCategory(request.category().trim());
        e.setNote(request.note());
    }

    static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
