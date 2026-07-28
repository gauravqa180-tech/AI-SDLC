package com.ai.sdlc.expensetracker.dashboard;

import com.ai.sdlc.expensetracker.budget.Budget;
import com.ai.sdlc.expensetracker.budget.BudgetService;
import com.ai.sdlc.expensetracker.dashboard.dto.DashboardResponse;
import com.ai.sdlc.expensetracker.expense.Expense;
import com.ai.sdlc.expensetracker.expense.ExpenseRepository;
import com.ai.sdlc.expensetracker.expense.ExpenseSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;

    public DashboardService(ExpenseRepository expenseRepository, BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.budgetService = budgetService;
    }

    public DashboardResponse dashboard(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        List<Expense> expenses = expenseRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "expenseDate"));

        Map<Long, DashboardResponse.CategoryBreakdown> breakdown = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Expense e : expenses) {
            total = total.add(e.getAmount());
            Long cid = e.getCategory().getId();
            DashboardResponse.CategoryBreakdown existing = breakdown.get(cid);
            if (existing == null) {
                breakdown.put(cid, new DashboardResponse.CategoryBreakdown(cid, e.getCategory().getName(), e.getAmount(), null, null));
            } else {
                breakdown.put(cid, new DashboardResponse.CategoryBreakdown(cid, existing.categoryName(), existing.spent().add(e.getAmount()), existing.budget(), existing.remaining()));
            }
        }

        Budget budget = budgetService.findOrNull(year, month);
        BigDecimal overallBudget = budget == null ? null : budget.getOverallAmount();
        BigDecimal overallRemaining = overallBudget == null ? null : overallBudget.subtract(total);

        Map<Long, BigDecimal> categoryBudgetMap = new HashMap<>();
        if (budget != null) {
            budget.getCategoryBudgets().forEach(cb -> categoryBudgetMap.put(cb.getCategory().getId(), cb.getAmount()));
        }

        List<DashboardResponse.CategoryBreakdown> categoryRows = breakdown.values().stream()
                .map(row -> {
                    BigDecimal b = categoryBudgetMap.get(row.categoryId());
                    BigDecimal remaining = b == null ? null : b.subtract(row.spent());
                    return new DashboardResponse.CategoryBreakdown(row.categoryId(), row.categoryName(), row.spent(), b, remaining);
                })
                .toList();

        return new DashboardResponse(year, month, total, overallBudget, overallRemaining, categoryRows);
    }
}
