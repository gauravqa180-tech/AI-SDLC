package com.ai.sdlc.expensetracker.insights.service;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.insights.api.dto.CategoryTotalResponse;
import com.ai.sdlc.expensetracker.insights.api.dto.MonthlyDashboardResponse;
import com.ai.sdlc.expensetracker.insights.api.dto.MonthlyDashboardResponse.*;
import com.ai.sdlc.expensetracker.budget.domain.Budget;
import com.ai.sdlc.expensetracker.budget.domain.CategoryBudget;
import com.ai.sdlc.expensetracker.budget.repo.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public MonthlyDashboardResponse monthlyDashboard(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        // Pull a capped list for aggregation. For production, use group-by queries.
        List<Expense> expenses = expenseRepository.findAll(
                (root, query, cb) -> cb.between(root.get("expenseDate"), from, to),
                PageRequest.of(0, 20000, Sort.by(Sort.Direction.DESC, "expenseDate"))
        ).getContent();

        BigDecimal monthlyTotal = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new HashMap<>();
        for (Expense e : expenses) {
            String cat = normalize(e.getCategory());
            byCategory.put(cat, byCategory.getOrDefault(cat, BigDecimal.ZERO).add(e.getAmount()));
        }

        List<CategoryTotalResponse> totalsByCategory = byCategory.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> new CategoryTotalResponse(e.getKey(), e.getValue()))
                .toList();

        BudgetSummary budgetSummary = buildBudgetSummary(month, monthlyTotal, byCategory);

        return new MonthlyDashboardResponse(month, monthlyTotal, totalsByCategory, budgetSummary);
    }

    private BudgetSummary buildBudgetSummary(YearMonth month, BigDecimal monthlyTotal, Map<String, BigDecimal> byCategory) {
        Optional<Budget> budgetOpt = budgetRepository.findByMonth(month);
        if (budgetOpt.isEmpty()) {
            return new BudgetSummary(null, null, null, List.of(), List.of());
        }

        Budget budget = budgetOpt.get();

        BigDecimal monthlyBudget = budget.getMonthlyBudget();
        BigDecimal remaining = monthlyBudget == null ? null : monthlyBudget.subtract(monthlyTotal);
        Integer percentUsed = monthlyBudget == null || monthlyBudget.compareTo(BigDecimal.ZERO) <= 0
                ? null
                : monthlyTotal.multiply(BigDecimal.valueOf(100)).divide(monthlyBudget, 0, RoundingMode.HALF_UP).intValue();

        List<CategoryBudgetLine> categoryLines = new ArrayList<>();
        for (CategoryBudget cb : budget.getCategoryBudgets()) {
            String cat = normalize(cb.getCategory());
            BigDecimal spent = byCategory.getOrDefault(cat, BigDecimal.ZERO);
            BigDecimal cbBudget = cb.getBudgetAmount();
            BigDecimal cbRemaining = cbBudget == null ? null : cbBudget.subtract(spent);
            Integer cbPercent = cbBudget == null || cbBudget.compareTo(BigDecimal.ZERO) <= 0
                    ? null
                    : spent.multiply(BigDecimal.valueOf(100)).divide(cbBudget, 0, RoundingMode.HALF_UP).intValue();
            categoryLines.add(new CategoryBudgetLine(cat, cbBudget, spent, cbRemaining, cbPercent));
        }
        categoryLines.sort((a, b) -> {
            int c = Integer.compare(Optional.ofNullable(b.percentUsed()).orElse(-1), Optional.ofNullable(a.percentUsed()).orElse(-1));
            if (c != 0) return c;
            return a.category().compareToIgnoreCase(b.category());
        });

        List<BudgetAlert> alerts = new ArrayList<>();
        // User Story 5: Alerts at 80% and 100%
        if (percentUsed != null) {
            if (percentUsed >= 100) {
                alerts.add(new BudgetAlert("MONTH", null, "EXCEEDED", "Monthly budget exceeded"));
            } else if (percentUsed >= 80) {
                alerts.add(new BudgetAlert("MONTH", null, "WARNING", "You have used " + percentUsed + "% of your monthly budget"));
            }
        }
        for (CategoryBudgetLine line : categoryLines) {
            Integer p = line.percentUsed();
            if (p == null) continue;
            if (p >= 100) {
                alerts.add(new BudgetAlert("CATEGORY", line.category(), "EXCEEDED", "Category budget exceeded for: " + line.category()));
            } else if (p >= 80) {
                alerts.add(new BudgetAlert("CATEGORY", line.category(), "WARNING", "You have used " + p + "% of budget for: " + line.category()));
            }
        }

        return new BudgetSummary(monthlyBudget, remaining, percentUsed, categoryLines, alerts);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
