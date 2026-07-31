package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(request.amount())
                .date(request.date())
                .category(request.category().trim())
                .note(request.note())
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(String q,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     String category,
                                     BigDecimal minAmount,
                                     BigDecimal maxAmount,
                                     Pageable pageable) {

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.search(q))
                .and(ExpenseSpecifications.dateGte(startDate))
                .and(ExpenseSpecifications.dateLte(endDate))
                .and(ExpenseSpecifications.categoryEq(category))
                .and(ExpenseSpecifications.amountGte(minAmount))
                .and(ExpenseSpecifications.amountLte(maxAmount));

        return expenseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return toResponse(expenseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Expense not found")));
    }

    // User Story #1: Edit expense
    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setCategory(request.category().trim());
        existing.setNote(request.note());
        return toResponse(existing);
    }

    // v1 delete
    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found");
        }
        expenseRepository.deleteById(id);
    }

    // User Story #2: Delete with undo (simple in-memory token)
    // Assumption: single instance / best-effort undo within ttl.
    private final Map<String, DeletedExpense> undoStore = new HashMap<>();

    @Transactional
    public String deleteWithUndo(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found"));
        expenseRepository.delete(expense);

        String token = UUID.randomUUID().toString();
        undoStore.put(token, new DeletedExpense(expense, System.currentTimeMillis() + 10_000));
        return token;
    }

    @Transactional
    public ExpenseResponse undoDelete(String token) {
        DeletedExpense deleted = undoStore.get(token);
        if (deleted == null || deleted.expiresAtMs < System.currentTimeMillis()) {
            undoStore.remove(token);
            throw new IllegalArgumentException("Undo token invalid or expired");
        }
        Expense expense = deleted.expense;
        expense.setId(null); // restore as new row with new id (simple approach)
        Expense saved = expenseRepository.save(expense);
        undoStore.remove(token);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal total = expenseRepository.findAllByDateBetween(start, end, Pageable.unpaged())
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyTotalResponse(month, total);
    }

    // User Story #5: Monthly breakdown by category
    @Transactional(readOnly = true)
    public MonthlyCategoryBreakdownResponse monthlyBreakdown(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findAllByDateBetween(start, end, Pageable.unpaged()).getContent();

        BigDecimal total = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Expense e : expenses) {
            byCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        List<CategoryBreakdownItem> items = byCategory.entrySet().stream()
                .map(e -> new CategoryBreakdownItem(e.getKey(), e.getValue()))
                .toList();

        return new MonthlyCategoryBreakdownResponse(month, total, items);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }

    private record DeletedExpense(Expense expense, long expiresAtMs) {
    }
}
