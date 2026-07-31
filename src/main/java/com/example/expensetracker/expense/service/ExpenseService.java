package com.example.expensetracker.expense.service;

import com.example.expensetracker.common.api.ResourceNotFoundException;
import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = Expense.builder()
                .amount(request.amount())
                .date(request.date())
                .category(request.category().trim())
                .note(request.note())
                .build();

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        return expenseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    /** US1: edit expense */
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense existing = findByIdOrThrow(id);
        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setCategory(request.category().trim());
        existing.setNote(request.note());
        return toResponse(existing);
    }

    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private Expense findByIdOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
