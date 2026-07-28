package com.ai.sdlc.expensetracker.budgets.service;

import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetAlertResponse;
import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budgets.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budgets.domain.Budget;
import com.ai.sdlc.expensetracker.budgets.domain.BudgetAlertState;
import com.ai.sdlc.expensetracker.budgets.repo.BudgetAlertStateRepository;
import com.ai.sdlc.expensetracker.budgets.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.expenses.domain.Expense;
import com.ai.sdlc.expensetracker.expenses.repo.ExpenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertStateRepository alertStateRepository;
    private final ExpenseRepository expenseRepository;

    private final BigDecimal warnThreshold;
    private final BigDecimal exceedThreshold;

    public BudgetService(BudgetRepository budgetRepository,
                         BudgetAlertStateRepository alertStateRepository,
                         ExpenseRepository expenseRepository,
                         @Value("${app.budgets.thresholds.warn:0.8}") BigDecimal warnThreshold,
                         @Value("${app.budgets.thresholds.exceed:1.0}") BigDecimal exceedThreshold) {
        this.budgetRepository = budgetRepository;
        this.alertStateRepository = alertStateRepository;
        this.expenseRepository = expenseRepository;
        this.warnThreshold = warnThreshold;
        this.exceedThreshold = exceedThreshold;
    }

    @Transactional
    public BudgetResponse upsert(YearMonth month, BudgetUpsertRequest req) {
        String m = month.toString();

        // overall
        Budget overall = budgetRepository.findByMonthAndCategory(m, null)
                .orElseGet(() -> new Budget(month, null, req.overallAmount()));
        overall.setAmount(req.overallAmount());
        budgetRepository.save(overall);

        // category budgets
        if (req.categoryBudgets() != null) {
            for (BudgetUpsertRequest.CategoryBudgetRequest cb : req.categoryBudgets()) {
                String cat = cb.category().trim();
                Budget b = budgetRepository.findByMonthAndCategory(m, cat)
                        .orElseGet(() -> new Budget(month, cat, cb.amount()));
                b.setAmount(cb.amount());
                budgetRepository.save(b);
            }
        }

        // Budget changed => reset alert states for that month (simple approach)
        resetAlertsForMonth(m);

        return get(month);
    }

    @Transactional(readOnly = true)
    public BudgetResponse get(YearMonth month) {
        String m = month.toString();
        List<Budget> budgets = budgetRepository.findByMonth(m);

        BigDecimal overall = budgets.stream()
                .filter(b -> b.getCategory() == null)
                .map(Budget::getAmount)
                .findFirst()
                .orElse(null);

        List<BudgetResponse.CategoryBudget> cats = budgets.stream()
                .filter(b -> b.getCategory() != null)
                .map(b -> new BudgetResponse.CategoryBudget(b.getCategory(), b.getAmount()))
                .sorted(Comparator.comparing(BudgetResponse.CategoryBudget::category))
                .toList();

        return new BudgetResponse(m, overall, cats);
    }

    @Transactional
    public List<BudgetAlertResponse> evaluateAlertsForMonth(YearMonth month) {
        String m = month.toString();
        List<Budget> budgets = budgetRepository.findByMonth(m);
        if (budgets.isEmpty()) return List.of();

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        Specification<Expense> inMonth = (root, query, cb) -> cb.between(root.get("date"), from, to);
        List<Expense> expenses = expenseRepository.findAll(inMonth);

        BigDecimal overallSpent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> spentByCategory = new HashMap<>();
        for (Expense e : expenses) {
            spentByCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        List<BudgetAlertResponse> alerts = new ArrayList<>();

        // overall
        budgets.stream().filter(b -> b.getCategory() == null).findFirst().ifPresent(b -> {
            alerts.addAll(checkAndMark(m, null, b.getAmount(), overallSpent));
        });

        // per-category
        for (Budget b : budgets) {
            if (b.getCategory() == null) continue;
            BigDecimal spent = spentByCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            alerts.addAll(checkAndMark(m, b.getCategory(), b.getAmount(), spent));
        }

        return alerts;
    }

    private List<BudgetAlertResponse> checkAndMark(String month, String category, BigDecimal budgetAmount, BigDecimal spent) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) return List.of();
        BigDecimal ratio = spent.divide(budgetAmount, 4, RoundingMode.HALF_UP);

        BudgetAlertState state = alertStateRepository.findByMonthAndCategory(month, category)
                .orElseGet(() -> new BudgetAlertState(month, category));

        List<BudgetAlertResponse> result = new ArrayList<>();

        if (!state.isWarnSent() && ratio.compareTo(warnThreshold) >= 0 && ratio.compareTo(exceedThreshold) < 0) {
            state.setWarnSent(true);
            result.add(new BudgetAlertResponse(month, category, "WARN_80", budgetAmount, spent, ratio));
        }

        if (!state.isExceedSent() && ratio.compareTo(exceedThreshold) >= 0) {
            state.setExceedSent(true);
            result.add(new BudgetAlertResponse(month, category, "EXCEED_100", budgetAmount, spent, ratio));
        }

        alertStateRepository.save(state);
        return result;
    }

    private void resetAlertsForMonth(String month) {
        // simple approach: delete and re-create on next evaluation
        // we don't have a custom delete-by-month method, so load + delete.
        // This is acceptable for small data volumes (v1).
        alertStateRepository.findAll().stream()
                .filter(s -> month.equals(s.getMonth()))
                .forEach(alertStateRepository::delete);
    }
}
