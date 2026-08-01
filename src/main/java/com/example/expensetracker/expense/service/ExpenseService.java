package com.example.expensetracker.expense.service;

import com.example.expensetracker.common.api.ExpenseNotFoundException;
import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = Expense.builder()
                .amount(request.amount())
                .expenseDate(request.date())
                .category(normalizeCategory(request.category()))
                .note(request.note())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));

        expense.setAmount(request.amount());
        expense.setExpenseDate(request.date());
        expense.setCategory(normalizeCategory(request.category()));
        expense.setNote(request.note());

        return toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate startDate,
                                     LocalDate endDate,
                                     String category,
                                     BigDecimal minAmount,
                                     BigDecimal maxAmount,
                                     String keyword,
                                     Sort sort) {
        Specification<Expense> spec = ExpenseSpecifications.build(startDate, endDate, category, minAmount, maxAmount, keyword);
        return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("expenseDate"), start, end);
        return expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getNote());
    }

    private String normalizeCategory(String category) {
        if (category == null) return "Uncategorized";
        String trimmed = category.trim();
        return trimmed.isEmpty() ? "Uncategorized" : trimmed;
    }
}
