package com.ai.sdlc.expensetracker.insights.service;

import com.ai.sdlc.expensetracker.expenses.domain.Expense;
import com.ai.sdlc.expensetracker.expenses.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.insights.api.dto.CategoryTotalResponse;
import com.ai.sdlc.expensetracker.insights.api.dto.MonthlyTrendPointResponse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class InsightsService {

    private final ExpenseRepository repository;

    public InsightsService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoryTotalResponse> categoryBreakdown(LocalDate from, LocalDate to) {
        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);
        List<Expense> expenses = repository.findAll(spec);

        Map<String, BigDecimal> totals = new HashMap<>();
        for (Expense e : expenses) {
            totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(e -> new CategoryTotalResponse(e.getKey(), e.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendPointResponse> monthlyTrend(int months) {
        int capped = Math.min(Math.max(months, 1), 24);
        YearMonth current = YearMonth.now();
        List<MonthlyTrendPointResponse> points = new ArrayList<>();

        for (int i = capped - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate from = ym.atDay(1);
            LocalDate to = ym.atEndOfMonth();
            Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);
            BigDecimal total = repository.findAll(spec).stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            points.add(new MonthlyTrendPointResponse(ym.toString(), total));
        }

        return points;
    }
}
