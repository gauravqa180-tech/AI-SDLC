package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.UpdateExpenseRequest;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse updateExpense(Long id, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: id=" + id));

        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(request.category().trim());
        expense.setNote(request.note());

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
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
