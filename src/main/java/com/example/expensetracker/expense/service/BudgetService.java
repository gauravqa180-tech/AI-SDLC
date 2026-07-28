package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.BudgetRequest;
import com.example.expensetracker.expense.domain.Budget;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.BudgetRepository;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BudgetService {

    public static final BigDecimal DEFAULT_WARN_THRESHOLD = new BigDecimal("0.80");

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public Budget upsert(BudgetRequest req) {
        Budget b = budgetRepository.findByMonthAndCategory(req.month(), normalizeCategory(req.category()))
                .orElseGet(Budget::new);
        b.setMonth(req.month());
        b.setCategory(normalizeCategory(req.category()));
        b.setAmount(req.amount());
        b.setWarnThresholdPct(req.warnThresholdPct());
        return budgetRepository.save(b);
    }

    @Transactional
    public void delete(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Budget not found");
        }
        budgetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Budget> listByMonth(YearMonth month) {
        return budgetRepository.findByMonth(month);
    }

    @Transactional(readOnly = true)
    public BudgetProgress progress(Long budgetId) {
        Budget b = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Budget not found"));

        YearMonth month = b.getMonth();
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));
        if (b.getCategory() != null) {
            spec = spec.and(ExpenseSpecifications.categoryEquals(b.getCategory()));
        }

        BigDecimal spent = expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amount = b.getAmount();
        BigDecimal remaining = amount.subtract(spent);
        BigDecimal pct = amount.signum() == 0 ? BigDecimal.ZERO : spent.divide(amount, 4, RoundingMode.HALF_UP);

        BigDecimal warn = b.getWarnThresholdPct() == null ? DEFAULT_WARN_THRESHOLD : b.getWarnThresholdPct();

        boolean warnReached = pct.compareTo(warn) >= 0;
        boolean exceeded = pct.compareTo(BigDecimal.ONE) >= 0;

        return new BudgetProgress(b.getId(), b.getMonth(), b.getCategory(), amount, spent, remaining, pct, warn, warnReached, exceeded,
                Map.of(
                        "WARN_80", warnReached,
                        "EXCEEDED_100", exceeded
                ));
    }

    private String normalizeCategory(String category) {
        if (category == null) return null;
        String c = category.trim();
        return c.isEmpty() ? null : c;
    }

    public record BudgetProgress(
            Long id,
            YearMonth month,
            String category,
            BigDecimal budgetAmount,
            BigDecimal spent,
            BigDecimal remaining,
            BigDecimal progressPct,
            BigDecimal warnThresholdPct,
            boolean warnReached,
            boolean exceeded,
            Map<String, Boolean> alerts
    ) {
    }
}
