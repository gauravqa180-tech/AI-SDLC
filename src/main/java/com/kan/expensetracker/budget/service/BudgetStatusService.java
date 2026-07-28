package com.kan.expensetracker.budget.service;

import com.kan.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.kan.expensetracker.budget.domain.Budget;
import com.kan.expensetracker.budget.domain.BudgetAlertState;
import com.kan.expensetracker.budget.repo.BudgetAlertStateRepository;
import com.kan.expensetracker.budget.repo.BudgetRepository;
import com.kan.expensetracker.expense.domain.Expense;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import com.kan.expensetracker.expense.service.ExpenseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetStatusService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertStateRepository budgetAlertStateRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetStatusResponse calculateStatus(int year, int month, boolean recordAlerts) {
        YearMonth ym = YearMonth.of(year, month);
        String monthStr = Budget.toMonthString(ym);

        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        List<Expense> expenses = expenseRepository.findAll(spec);
        BigDecimal totalSpent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, BigDecimal> spentByCategoryId = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getCategory().getId(), Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        Budget overallBudget = budgetRepository.findByBudgetMonthAndCategoryIsNull(monthStr).orElse(null);

        List<BudgetStatusResponse.BudgetProgress> byCategory = new ArrayList<>();
        List<Budget> budgets = budgetRepository.findAllByBudgetMonth(monthStr);
        for (Budget b : budgets) {
            if (b.getCategory() == null) continue;
            BigDecimal spent = spentByCategoryId.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO);
            byCategory.add(new BudgetStatusResponse.BudgetProgress(
                    b.getId(),
                    b.getCategory().getId(),
                    b.getCategory().getName(),
                    spent,
                    b.getAmount(),
                    b.getAmount().subtract(spent)
            ));
        }

        BudgetStatusResponse.BudgetProgress overall = null;
        if (overallBudget != null) {
            overall = new BudgetStatusResponse.BudgetProgress(
                    overallBudget.getId(),
                    null,
                    "OVERALL",
                    totalSpent,
                    overallBudget.getAmount(),
                    overallBudget.getAmount().subtract(totalSpent)
            );
        }

        List<BudgetStatusResponse.BudgetAlert> alerts = new ArrayList<>();
        if (recordAlerts) {
            if (overallBudget != null) {
                alerts.addAll(checkAndRecord(monthStr, null, "OVERALL", totalSpent, overallBudget));
            }
            for (Budget b : budgets) {
                if (b.getCategory() == null) continue;
                BigDecimal spent = spentByCategoryId.getOrDefault(b.getCategory().getId(), BigDecimal.ZERO);
                alerts.addAll(checkAndRecord(monthStr, b.getCategory().getId(), b.getCategory().getName(), spent, b));
            }
        }

        return new BudgetStatusResponse(year, month, overall, byCategory, alerts);
    }

    private List<BudgetStatusResponse.BudgetAlert> checkAndRecord(
            String monthStr,
            Long categoryId,
            String categoryName,
            BigDecimal spent,
            Budget budget
    ) {
        List<BudgetStatusResponse.BudgetAlert> alerts = new ArrayList<>();
        if (budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) return alerts;

        BigDecimal warnAt = budget.getAmount().multiply(budget.getThresholdWarn());
        BigDecimal exceedAt = budget.getAmount().multiply(budget.getThresholdExceed());

        if (spent.compareTo(warnAt) >= 0) {
            if (tryRecordAlert(monthStr, categoryId, BudgetAlertState.ThresholdType.WARN_80)) {
                alerts.add(new BudgetStatusResponse.BudgetAlert(scope(categoryId), categoryId, categoryName, "WARN", spent, budget.getAmount()));
            }
        }

        if (spent.compareTo(exceedAt) >= 0) {
            if (tryRecordAlert(monthStr, categoryId, BudgetAlertState.ThresholdType.EXCEED_100)) {
                alerts.add(new BudgetStatusResponse.BudgetAlert(scope(categoryId), categoryId, categoryName, "EXCEED", spent, budget.getAmount()));
            }
        }

        return alerts;
    }

    private boolean tryRecordAlert(String monthStr, Long categoryId, BudgetAlertState.ThresholdType thresholdType) {
        boolean exists = (categoryId == null)
                ? budgetAlertStateRepository.findByBudgetMonthAndCategoryIsNullAndThresholdType(monthStr, thresholdType).isPresent()
                : budgetAlertStateRepository.findByBudgetMonthAndCategory_IdAndThresholdType(monthStr, categoryId, thresholdType).isPresent();
        if (exists) return false;

        BudgetAlertState st = BudgetAlertState.builder()
                .budgetMonth(monthStr)
                .category(null)
                .thresholdType(thresholdType)
                .triggeredAt(Instant.now())
                .build();

        // if categoryId != null we need to set a reference. To keep service dependency minimal, we store null category reference
        // and rely on unique constraint with nullable category_id being different for categories. But MySQL unique with null
        // allows multiple nulls, so for category-specific alerts we MUST set category. We'll load through EntityManager via repository? 
        // Instead, we will store categoryId using a lightweight approach: create a proxy Category with id only.
        if (categoryId != null) {
            var c = new com.kan.expensetracker.category.domain.Category();
            c.setId(categoryId);
            st.setCategory(c);
        }

        budgetAlertStateRepository.save(st);
        return true;
    }

    private static String scope(Long categoryId) {
        return categoryId == null ? "OVERALL" : "CATEGORY";
    }
}
