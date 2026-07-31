package com.aisdlc.expensetracker.service;

import com.aisdlc.expensetracker.api.dto.CreateExpenseRequest;
import com.aisdlc.expensetracker.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.api.dto.UpdateExpenseRequest;
import com.aisdlc.expensetracker.domain.Expense;
import com.aisdlc.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static com.aisdlc.expensetracker.service.ExpenseSpecifications.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    /**
     * Soft-delete undo window in seconds.
     */
    private final long undoWindowSeconds;

    public ExpenseService(ExpenseRepository expenseRepository,
                          @Value("${expense.undo.windowSeconds:30}") long undoWindowSeconds) {
        this.expenseRepository = expenseRepository;
        this.undoWindowSeconds = undoWindowSeconds;
    }

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        Expense expense = new Expense();
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category());
        expense.setNote(request.note());
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(LocalDate startDate,
                                     LocalDate endDate,
                                     String category,
                                     BigDecimal minAmount,
                                     BigDecimal maxAmount,
                                     String q,
                                     int page,
                                     int size,
                                     String sortField,
                                     Sort.Direction sortOrder) {

        Specification<Expense> spec = Specification.where(notDeleted());

        if (startDate != null && endDate != null) {
            spec = spec.and(dateBetween(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), startDate));
        } else if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        if (category != null && !category.isBlank()) {
            spec = spec.and(categoryEquals(category));
        }
        if (minAmount != null) {
            spec = spec.and(amountGte(minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and(amountLte(maxAmount));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(noteContainsIgnoreCase(q));
        }

        Sort sort = Sort.by(sortOrder == null ? Sort.Direction.DESC : sortOrder,
                (sortField == null || sortField.isBlank()) ? "date" : sortField);

        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), sort);
        return expenseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findById(id)
                .filter(exp -> exp.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        return toResponse(e);
    }

    @Transactional
    public ExpenseResponse update(Long id, UpdateExpenseRequest request) {
        Expense e = expenseRepository.findById(id)
                .filter(exp -> exp.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));

        e.setAmount(request.amount());
        e.setDate(request.date());
        e.setCategory(request.category());
        e.setNote(request.note());

        Expense saved = expenseRepository.save(e);
        return toResponse(saved);
    }

    /**
     * Soft-delete to support undo.
     */
    @Transactional
    public void delete(Long id) {
        Expense e = expenseRepository.findById(id)
                .filter(exp -> exp.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        e.setDeletedAt(Instant.now());
        expenseRepository.save(e);
    }

    /**
     * Restore if within undo window.
     */
    @Transactional
    public ExpenseResponse undoDelete(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        if (e.getDeletedAt() == null) {
            return toResponse(e);
        }

        Instant cutoff = Instant.now().minusSeconds(undoWindowSeconds);
        if (e.getDeletedAt().isBefore(cutoff)) {
            throw new IllegalStateException("Undo window expired");
        }
        e.setDeletedAt(null);
        Expense saved = expenseRepository.save(e);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Specification<Expense> spec = Specification.where(notDeleted()).and(dateBetween(start, end));
        List<Expense> expenses = expenseRepository.findAll(spec);
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 200);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory(),
                e.getNote(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
