package com.example.expensetracker.budget.service;

import com.example.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.example.expensetracker.budget.api.dto.BudgetResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.domain.Budget;
import com.example.expensetracker.budget.repo.BudgetRepository;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest request) {
        YearMonth month = YearMonth.parse(request.month());
        String category = normalizeCategory(request.category());

        Budget budget = budgetRepository.findByBudgetMonthAndCategory(month, category)
                .orElseGet(() -> Budget.builder().budgetMonth(month).category(category).build());

        budget.setAmount(request.amount());
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(String month) {
        YearMonth ym = YearMonth.parse(month);
        return budgetRepository.findAllByBudgetMonth(ym).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponse progress(String month, String categoryRaw) {
        YearMonth ym = YearMonth.parse(month);
        String category = normalizeCategory(categoryRaw);

        Budget budget = budgetRepository.findByBudgetMonthAndCategory(ym, category)
                .orElse(null);
        if (budget == null) {
            return new BudgetProgressResponse(month, category, null, BigDecimal.ZERO, null, 0, BudgetProgressResponse.AlertLevel.NONE);
        }

        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal spent;
        if (category == null) {
            spent = expenseRepository.totalForRange(from, to);
        } else {
            // reuse search aggregation quickly using query: total for range + category
            spent = expenseRepository.search(from, to, category, null,
                            org.springframework.data.domain.PageRequest.of(0, 1))
                    .stream()
                    .map(com.example.expensetracker.expense.domain.Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal remaining = budget.getAmount().subtract(spent);
        int percentUsed = budget.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                spent.multiply(HUNDRED).divide(budget.getAmount(), 0, RoundingMode.HALF_UP).intValue();

        BudgetProgressResponse.AlertLevel alert = BudgetProgressResponse.AlertLevel.NONE;
        if (percentUsed >= 100) alert = BudgetProgressResponse.AlertLevel.EXCEEDED_100;
        else if (percentUsed >= 80) alert = BudgetProgressResponse.AlertLevel.WARNING_80;

        return new BudgetProgressResponse(month, category, budget.getAmount(), spent, remaining, percentUsed, alert);
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getBudgetMonth().toString(), b.getCategory(), b.getAmount());
    }

    private String normalizeCategory(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
