package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.domain.Budget;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Value("${app.budgets.warnThresholdPercent:80}")
    private int warnThresholdPercent;

    @Value("${app.budgets.overThresholdPercent:100}")
    private int overThresholdPercent;

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest request) {
        String category = normalizeCategory(request.category());
        Budget budget = budgetRepository.findByMonthAndCategory(request.month(), category)
                .orElseGet(() -> Budget.builder().month(request.month()).category(category).build());

        budget.setAmount(request.amount());
        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listByMonth(String month) {
        return budgetRepository.findByMonth(month).stream().map(BudgetService::toResponse).toList();
    }

    @Transactional
    public void delete(long id) {
        if (!budgetRepository.existsById(id)) {
            throw new EntityNotFoundException("Budget not found: " + id);
        }
        budgetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponse progress(String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal overallSpent = expenseRepository.sumByDateRange(from, to);

        List<Budget> budgets = budgetRepository.findByMonth(month);
        Budget overall = budgets.stream().filter(b -> b.getCategory() == null).findFirst().orElse(null);

        BigDecimal overallBudget = overall == null ? null : overall.getAmount();
        BigDecimal overallRemaining = overallBudget == null ? null : overallBudget.subtract(overallSpent);
        String overallStatus = overallBudget == null ? "NO_BUDGET" : status(overallSpent, overallBudget);

        Map<String, BigDecimal> spendByCategory = new HashMap<>();
        for (Object[] row : expenseRepository.sumByCategoryInDateRange(from, to)) {
            spendByCategory.put((String) row[0], (BigDecimal) row[1]);
        }

        List<BudgetProgressCategoryItem> categoryItems = budgets.stream()
                .filter(b -> b.getCategory() != null)
                .map(b -> {
                    BigDecimal spent = spendByCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO);
                    BigDecimal remaining = b.getAmount().subtract(spent);
                    return new BudgetProgressCategoryItem(
                            b.getCategory(),
                            b.getAmount(),
                            spent,
                            remaining,
                            status(spent, b.getAmount())
                    );
                })
                .sorted(Comparator.comparing(BudgetProgressCategoryItem::category))
                .toList();

        return new BudgetProgressResponse(
                month,
                overallBudget,
                overallSpent,
                overallRemaining,
                overallStatus,
                categoryItems,
                warnThresholdPercent,
                overThresholdPercent
        );
    }

    private String status(BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) return "NO_BUDGET";
        BigDecimal pct = spent.multiply(BigDecimal.valueOf(100)).divide(budget, 2, java.math.RoundingMode.HALF_UP);
        if (pct.compareTo(BigDecimal.valueOf(overThresholdPercent)) >= 0) return "OVER";
        if (pct.compareTo(BigDecimal.valueOf(warnThresholdPercent)) >= 0) return "WARN";
        return "OK";
    }

    private static String normalizeCategory(String category) {
        if (category == null) return null;
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getMonth(), b.getCategory(), b.getAmount());
    }
}
