package com.example.expensetracker.budget.service;

import com.example.expensetracker.budget.api.dto.BudgetRequest;
import com.example.expensetracker.budget.api.dto.BudgetStatusResponse;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public Budget upsert(BudgetRequest request) {
        String normalizedCategory = normalizeCategory(request.category());
        Budget budget = budgetRepository.findByMonthAndCategory(request.month(), normalizedCategory)
                .orElseGet(Budget::new);

        budget.setMonth(request.month());
        budget.setCategory(normalizedCategory);
        budget.setAmount(request.amount());

        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<Budget> getBudgets(String month) {
        return budgetRepository.findByMonth(month);
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse status(String month) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal overallSpent = expenseRepository.sumAmountBetween(start, end);
        List<ExpenseRepository.CategoryTotalRow> categorySpentRows = expenseRepository.sumByCategoryBetween(start, end);

        List<Budget> budgets = budgetRepository.findByMonth(month);
        Map<String, BigDecimal> categoryBudgets = new HashMap<>();
        BigDecimal overallBudget = null;

        for (Budget b : budgets) {
            if (b.getCategory() == null) {
                overallBudget = b.getAmount();
            } else {
                categoryBudgets.put(b.getCategory(), b.getAmount());
            }
        }

        List<BudgetStatusResponse.Alert> alerts = new ArrayList<>();
        Integer overallPercentUsed = percent(overallSpent, overallBudget);
        BigDecimal overallRemaining = overallBudget == null ? null : overallBudget.subtract(overallSpent);
        if (overallBudget != null) {
            if (overallSpent.compareTo(overallBudget) > 0) {
                alerts.add(new BudgetStatusResponse.Alert("EXCEEDED", "You have exceeded your overall budget for " + month));
            } else if (overallSpent.compareTo(overallBudget.multiply(new BigDecimal("0.80"))) >= 0) {
                alerts.add(new BudgetStatusResponse.Alert("NEAR", "You have reached 80% of your overall budget for " + month));
            }
        }

        Map<String, BigDecimal> categorySpent = new HashMap<>();
        for (ExpenseRepository.CategoryTotalRow row : categorySpentRows) {
            categorySpent.put(row.getCategory(), row.getTotal());
        }

        Set<String> categories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        categories.addAll(categoryBudgets.keySet());
        categories.addAll(categorySpent.keySet());

        List<BudgetStatusResponse.CategoryStatus> categoryStatuses = new ArrayList<>();
        for (String cat : categories) {
            BigDecimal b = categoryBudgets.get(cat);
            BigDecimal s = categorySpent.getOrDefault(cat, BigDecimal.ZERO);
            BigDecimal r = b == null ? null : b.subtract(s);
            Integer p = percent(s, b);
            if (b != null) {
                if (s.compareTo(b) > 0) {
                    alerts.add(new BudgetStatusResponse.Alert("EXCEEDED", "You have exceeded your budget for category '" + cat + "' in " + month));
                } else if (s.compareTo(b.multiply(new BigDecimal("0.80"))) >= 0) {
                    alerts.add(new BudgetStatusResponse.Alert("NEAR", "You have reached 80% of your budget for category '" + cat + "' in " + month));
                }
            }
            categoryStatuses.add(new BudgetStatusResponse.CategoryStatus(cat, b, s, r, p));
        }

        return new BudgetStatusResponse(
                month,
                overallBudget,
                overallSpent,
                overallRemaining,
                overallPercentUsed,
                alerts,
                categoryStatuses
        );
    }

    private static Integer percent(BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) == 0) return null;
        return spent.multiply(new BigDecimal("100"))
                .divide(budget, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private static String normalizeCategory(String category) {
        if (category == null) return null;
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
