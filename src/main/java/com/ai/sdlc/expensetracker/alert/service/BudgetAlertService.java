package com.ai.sdlc.expensetracker.alert.service;

import com.ai.sdlc.expensetracker.alert.domain.BudgetAlert;
import com.ai.sdlc.expensetracker.alert.repo.BudgetAlertRepository;
import com.ai.sdlc.expensetracker.budget.domain.Budget;
import com.ai.sdlc.expensetracker.budget.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    public static final BigDecimal THRESHOLD_80 = new BigDecimal("0.80");
    public static final BigDecimal THRESHOLD_100 = new BigDecimal("1.00");

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetAlertRepository budgetAlertRepository;

    /**
     * Evaluate alerts for a given month/category. Creates one-time alerts per threshold.
     */
    @Transactional
    public void evaluateThresholds(YearMonth month, String category) {
        String m = month.toString();
        String c = category.trim();

        Budget budget = budgetRepository.findByMonthAndCategory(m, c).orElse(null);
        if (budget == null) {
            return;
        }

        BigDecimal spent = spentFor(month, c);
        if (budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal ratio = spent.divide(budget.getAmount(), 4, java.math.RoundingMode.HALF_UP);

        maybeCreateAlert(m, c, THRESHOLD_80, ratio, budget.getAmount(), spent);
        maybeCreateAlert(m, c, THRESHOLD_100, ratio, budget.getAmount(), spent);
    }

    @Transactional(readOnly = true)
    public java.util.List<BudgetAlert> listAlerts(YearMonth month) {
        return budgetAlertRepository.findAllByMonthOrderByCategoryAscThresholdAsc(month.toString());
    }

    private void maybeCreateAlert(String month, String category, BigDecimal threshold, BigDecimal ratio,
                                  BigDecimal budgetAmount, BigDecimal spentAmount) {
        if (ratio.compareTo(threshold) < 0) {
            return;
        }
        if (budgetAlertRepository.existsByMonthAndCategoryAndThreshold(month, category, threshold)) {
            return;
        }

        BudgetAlert alert = new BudgetAlert();
        alert.setMonth(month);
        alert.setCategory(category);
        alert.setThreshold(threshold);
        alert.setBudgetAmount(budgetAmount);
        alert.setSpentAmount(spentAmount);
        budgetAlertRepository.save(alert);
    }

    private BigDecimal spentFor(YearMonth month, String category) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        Specification<Expense> spec = Specification.where((root, query, cb) -> cb.between(root.get("date"), start, end))
                .and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));

        return expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
