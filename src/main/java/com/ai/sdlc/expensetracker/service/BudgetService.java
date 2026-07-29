package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.BudgetStatusResponse;
import com.ai.sdlc.expensetracker.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.domain.Budget;
import com.ai.sdlc.expensetracker.repository.BudgetRepository;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public BudgetStatusResponse upsert(BudgetUpsertRequest req) {
        String period = req.period().trim();
        String category = (req.category() == null || req.category().isBlank()) ? null : req.category().trim();

        Budget b = budgetRepository.findByPeriodAndCategory(period, category)
                .orElseGet(() -> Budget.builder().period(period).category(category).build());

        b.setLimitAmount(req.limitAmount());
        b.setWarnThresholdPercent(req.warnThresholdPercent());
        b = budgetRepository.save(b);

        return status(period, category);
    }

    @Transactional(readOnly = true)
    public BudgetStatusResponse status(String period, String category) {
        String p = period.trim();
        String c = (category == null || category.isBlank()) ? null : category.trim();

        Budget b = budgetRepository.findByPeriodAndCategory(p, c)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found for period=" + p + " category=" + c));

        YearMonth ym = YearMonth.parse(p);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal spent;
        if (c == null) {
            spent = expenseRepository.sumByDateRange(from, to);
        } else {
            spent = expenseRepository.findAll(ExpenseRepository.withFilters(null, c, from, to, null, null))
                    .stream()
                    .map(e -> e.getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal pct = BigDecimal.ZERO;
        if (b.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            pct = spent.multiply(BigDecimal.valueOf(100)).divide(b.getLimitAmount(), 2, RoundingMode.HALF_UP);
        }

        boolean warn = pct.compareTo(b.getWarnThresholdPercent()) >= 0;
        boolean exceeded = pct.compareTo(BigDecimal.valueOf(100)) >= 0;

        return new BudgetStatusResponse(b.getPeriod(), b.getCategory(), b.getLimitAmount(), b.getWarnThresholdPercent(), spent, pct, warn, exceeded);
    }
}
