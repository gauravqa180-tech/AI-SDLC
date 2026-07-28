package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.AlertResponse;
import com.ai.sdlc.expensetracker.domain.AlertEvent;
import com.ai.sdlc.expensetracker.domain.Budget;
import com.ai.sdlc.expensetracker.repo.AlertEventRepository;
import com.ai.sdlc.expensetracker.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final AlertEventRepository alertEventRepository;

    @Value("${app.budgets.threshold-80:0.80}")
    private double threshold80;

    @Value("${app.budgets.threshold-100:1.00}")
    private double threshold100;

    @Transactional
    public List<AlertResponse> evaluateAndCreateAlerts(LocalDate expenseDate) {
        YearMonth ym = YearMonth.from(expenseDate);
        String month = ym.toString();

        List<AlertResponse> alerts = new ArrayList<>();
        alerts.addAll(evaluateOverall(month, ym));
        alerts.addAll(evaluateCategories(month, ym));
        return alerts;
    }

    private List<AlertResponse> evaluateOverall(String month, YearMonth ym) {
        Budget overall = budgetRepository.findByMonthAndCategory(month, null).orElse(null);
        if (overall == null) {
            return List.of();
        }

        BigDecimal spent = expenseRepository.sumAmountBetween(ym.atDay(1), ym.atEndOfMonth());
        return createThresholdAlerts(month, "OVERALL", null, spent, overall.getAmount());
    }

    private List<AlertResponse> evaluateCategories(String month, YearMonth ym) {
        List<AlertResponse> alerts = new ArrayList<>();
        List<Budget> budgets = budgetRepository.findByMonth(month);
        for (Budget b : budgets) {
            if (b.getCategory() == null || b.getCategory().isBlank()) {
                continue;
            }

            BigDecimal spent = sumCategory(ym, b.getCategory());
            alerts.addAll(createThresholdAlerts(month, "CATEGORY", b.getCategory(), spent, b.getAmount()));
        }
        return alerts;
    }

    private BigDecimal sumCategory(YearMonth ym, String category) {
        return expenseRepository.sumByCategoryBetween(ym.atDay(1), ym.atEndOfMonth()).stream()
                .filter(r -> category.equals(r[0]))
                .map(r -> (BigDecimal) r[1])
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private List<AlertResponse> createThresholdAlerts(String month, String scope, String category, BigDecimal spent, BigDecimal budget) {
        if (budget == null || budget.signum() <= 0) {
            return List.of();
        }

        List<AlertResponse> alerts = new ArrayList<>();
        alerts.addAll(maybeCreate(month, scope, category, threshold80, spent, budget));
        alerts.addAll(maybeCreate(month, scope, category, threshold100, spent, budget));
        return alerts;
    }

    private List<AlertResponse> maybeCreate(String month, String scope, String category, double threshold, BigDecimal spent, BigDecimal budget) {
        BigDecimal limit = budget.multiply(BigDecimal.valueOf(threshold));
        if (spent.compareTo(limit) < 0) {
            return List.of();
        }

        Optional<AlertEvent> existing = alertEventRepository.findByMonthAndScopeAndCategoryAndThreshold(month, scope, category, threshold);
        if (existing.isPresent()) {
            return List.of();
        }

        alertEventRepository.save(AlertEvent.builder()
                .month(month)
                .scope(scope)
                .category(category)
                .threshold(threshold)
                .createdAt(Instant.now())
                .build());

        String msg = buildMessage(scope, category, threshold, spent, budget);
        return List.of(new AlertResponse(month, scope, category, threshold, msg, spent, budget));
    }

    private String buildMessage(String scope, String category, double threshold, BigDecimal spent, BigDecimal budget) {
        String pct = (int) Math.round(threshold * 100) + "%";
        String what = "OVERALL".equals(scope) ? "monthly" : ("'" + category + "' category");
        if (threshold >= 1.0) {
            return "You have exceeded your " + what + " budget. Spent " + spent.setScale(2) + " of " + budget.setScale(2) + ".";
        }
        return "You have reached " + pct + " of your " + what + " budget. Spent " + spent.setScale(2) + " of " + budget.setScale(2) + ".";
    }
}
