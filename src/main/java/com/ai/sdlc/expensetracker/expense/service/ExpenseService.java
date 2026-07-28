package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.common.api.NotFoundException;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense create(ExpenseCreateRequest req) {
        Expense e = Expense.builder()
                .amount(req.amount())
                .expenseDate(req.date())
                .category(req.category().trim())
                .note(req.note())
                .build();
        return expenseRepository.save(e);
    }

    @Transactional
    public Expense update(long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        return expenseRepository.save(e);
    }

    @Transactional
    public void delete(long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public Expense get(long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }

    public Page<Expense> list(LocalDate from, LocalDate to, String category, String q, Pageable pageable) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.noteContains(q));
        return expenseRepository.findAll(spec, pageable);
    }
}
