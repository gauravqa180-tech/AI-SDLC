package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = Expense.builder()
                .amount(request.amount())
                .date(request.date())
                .category(request.category())
                .note(request.note())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));

        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category());
        expense.setNote(request.note());

        return toResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from,
                                     LocalDate to,
                                     String category,
                                     String q,
                                     Sort sort) {

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.noteContainsIgnoreCase(q));

        return expenseRepository.findAll(spec, sort)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyCategoryBreakdownResponse monthlyCategoryBreakdown(YearMonth month) {
        YearMonth effectiveMonth = (month == null) ? YearMonth.now() : month;
        LocalDate from = effectiveMonth.atDay(1);
        LocalDate to = effectiveMonth.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findAll(
                Specification.where(ExpenseSpecifications.dateFrom(from))
                        .and(ExpenseSpecifications.dateTo(to)),
                Sort.by(Sort.Direction.ASC, "category")
        );

        Map<String, BigDecimal> totals = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.mapping(Expense::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        BigDecimal monthlyTotal = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MonthlyCategoryBreakdownItem> items = totals.entrySet().stream()
                .map(e -> new MonthlyCategoryBreakdownItem(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MonthlyCategoryBreakdownItem::total).reversed())
                .toList();

        return new MonthlyCategoryBreakdownResponse(effectiveMonth, monthlyTotal, items);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDate(),
                expense.getCategory(),
                expense.getNote()
        );
    }
}
