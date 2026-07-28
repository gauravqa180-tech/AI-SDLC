package com.ai.sdlc.expensetracker.expenses.service;

import com.ai.sdlc.expensetracker.common.api.NotFoundException;
import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expenses.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expenses.domain.Expense;
import com.ai.sdlc.expensetracker.expenses.repo.ExpenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense expense = new Expense(req.amount(), req.date(), req.category().trim(), req.note());
        Expense saved = repository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Optional<String> q,
                                     Optional<LocalDate> dateFrom,
                                     Optional<LocalDate> dateTo,
                                     Optional<String> category,
                                     Optional<BigDecimal> minAmount,
                                     Optional<BigDecimal> maxAmount,
                                     Sort sort) {
        Specification<Expense> spec = ExpenseSpecifications.all(q, dateFrom, dateTo, category, minAmount, maxAmount);
        return repository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        expense.setAmount(req.amount());
        expense.setDate(req.date());
        expense.setCategory(req.category().trim());
        expense.setNote(req.note());

        return toResponse(expense);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        repository.deleteById(id);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
