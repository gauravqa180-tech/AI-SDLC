package com.aisdlc.expensetracker.expense.service;

import com.aisdlc.expensetracker.common.api.NotFoundException;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.domain.Expense;
import com.aisdlc.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense create(ExpenseRequest req) {
        Expense e = Expense.builder()
                .amount(req.amount())
                .expenseDate(req.date())
                .category(req.category().trim())
                .note(req.note() == null ? null : req.note().trim())
                .deleted(false)
                .deletedAt(null)
                .build();
        return expenseRepository.save(e);
    }

    @Transactional(readOnly = true)
    public Page<Expense> list(Optional<LocalDate> from, Optional<LocalDate> to, Optional<String> category,
                             Optional<String> q, Pageable pageable) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.isActive());
        if (from.isPresent()) spec = spec.and(ExpenseSpecifications.dateGte(from.get()));
        if (to.isPresent()) spec = spec.and(ExpenseSpecifications.dateLte(to.get()));
        if (category.isPresent() && !category.get().isBlank()) spec = spec.and(ExpenseSpecifications.categoryEquals(category.get().trim()));
        if (q.isPresent() && !q.get().isBlank()) spec = spec.and(ExpenseSpecifications.noteContains(q.get().trim()));
        return expenseRepository.findAll(spec, pageable);
    }

    @Transactional
    public Expense update(long id, ExpenseRequest req) {
        Expense e = getActiveById(id);
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note() == null ? null : req.note().trim());
        return expenseRepository.save(e);
    }

    @Transactional
    public void softDelete(long id) {
        Expense e = getActiveById(id);
        e.setDeleted(true);
        e.setDeletedAt(OffsetDateTime.now());
        expenseRepository.save(e);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return expenseRepository.sumBetween(from, to);
    }

    private Expense getActiveById(long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        if (e.isDeleted()) {
            throw new NotFoundException("Expense not found: " + id);
        }
        return e;
    }
}
