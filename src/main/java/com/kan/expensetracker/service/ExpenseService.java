package com.kan.expensetracker.service;

import com.kan.expensetracker.api.dto.ExpenseCreateRequest;
import com.kan.expensetracker.api.dto.ExpenseResponse;
import com.kan.expensetracker.api.dto.ExpenseUpdateRequest;
import com.kan.expensetracker.domain.Expense;
import com.kan.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = new Expense(request.amount(), request.date(), request.category(), request.note());
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        return expenseRepository.findAll().stream()
                .filter(e -> !e.isDeleted())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category());
        expense.setNote(request.note());

        return toResponse(expense);
    }

    /**
     * Soft delete to support undo.
     */
    @Transactional
    public void softDelete(Long id) {
        Expense expense = expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        expense.softDelete(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Transactional
    public ExpenseResponse restore(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        if (!expense.isDeleted()) {
            return toResponse(expense);
        }

        expense.restore();
        return toResponse(expense);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
