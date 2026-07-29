package com.example.expensetracker.budget.service;

import com.example.expensetracker.budget.api.dto.BudgetAlertResponse;
import com.example.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.example.expensetracker.budget.api.dto.BudgetResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.domain.Budget;
import com.example.expensetracker.budget.domain.BudgetAlertState;
import com.example.expensetracker.budget.repo.BudgetAlertStateRepository;
import com.example.expensetracker.budget.repo.BudgetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAlertStateRepository alertStateRepository;
    private final EntityManager em;

    private final BigDecimal threshold80;
    private final BigDecimal threshold100;

    public BudgetService(BudgetRepository budgetRepository,
                         BudgetAlertStateRepository alertStateRepository,
                         EntityManager em,
                         @Value("${app.budgets.threshold80:0.8}") BigDecimal threshold80,
                         @Value("${app.budgets.threshold100:1.0}") BigDecimal threshold100) {
        this.budgetRepository = budgetRepository;
        this.alertStateRepository = alertStateRepository;
        this.em = em;
        this.threshold80 = threshold80;
        this.threshold100 = threshold100;
    }

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest req) {
        YearMonth ym = YearMonth.of(req.year(), req.month());
        String month = ym.toString();
        String category = req.category().trim();

        Budget budget = budgetRepository.findByBudgetMonthAndCategory(month, category)
                .orElseGet(Budget::new);
        budget.setMonth(ym);
        budget.setCategory(category);
        budget.setAmount(req.amount());

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new EntityNotFoundException("Budget not found: " + id);
        }
        budgetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(int year, int month) {
        String ym = YearMonth.of(year, month).toString();
        return budgetRepository.findAllByBudgetMonth(ym).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponse progress(int year, int month, String category) {
        YearMonth ym = YearMonth.of(year, month);
        String m = ym.toString();
        String c = category.trim();

        Budget budget = budgetRepository.findByBudgetMonthAndCategory(m, c)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found for " + m + " / " + c));

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal spent = sumCategoryBetween(c, start, end);
        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal ratio = spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP);

        return new BudgetProgressResponse(m, c, budget.getAmount(), spent, remaining, ratio);
    }

    /**
     * US5: budget threshold alerts. Called after creating/updating an expense.
     * Returns optional alert to show in-app.
     */
    @Transactional
    public Optional<BudgetAlertResponse> evaluateAlert(LocalDate expenseDate, String category) {
        YearMonth ym = YearMonth.from(expenseDate);
        String month = ym.toString();
        String c = category.trim();

        Optional<Budget> budgetOpt = budgetRepository.findByBudgetMonthAndCategory(month, c);
        if (budgetOpt.isEmpty()) {
            return Optional.empty();
        }

        Budget budget = budgetOpt.get();
        BigDecimal budgetAmount = budget.getAmount();
        if (budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        BigDecimal spent = sumCategoryBetween(c, start, end);
        BigDecimal ratio = spent.divide(budgetAmount, 4, RoundingMode.HALF_UP);

        BudgetAlertState state = alertStateRepository.findByBudgetMonthAndCategory(month, c)
                .orElseGet(() -> {
                    BudgetAlertState s = new BudgetAlertState();
                    s.setMonth(ym);
                    s.setCategory(c);
                    return s;
                });

        boolean changed = false;

        if (!state.isNotified100() && ratio.compareTo(threshold100) >= 0) {
            state.setNotified100(true);
            changed = true;
            alertStateRepository.save(state);
            return Optional.of(new BudgetAlertResponse(month, c, 100, "Budget exceeded for category: " + c));
        }

        if (!state.isNotified80() && ratio.compareTo(threshold80) >= 0) {
            state.setNotified80(true);
            changed = true;
            alertStateRepository.save(state);
            return Optional.of(new BudgetAlertResponse(month, c, 80, "Budget nearly reached for category: " + c));
        }

        if (changed) {
            alertStateRepository.save(state);
        }

        return Optional.empty();
    }

    private BigDecimal sumCategoryBetween(String category, LocalDate start, LocalDate end) {
        List<Tuple> rows = em.createQuery(
                        "select coalesce(sum(e.amount), 0) as total from Expense e " +
                                "where e.deleted = false and e.category = :category and e.date >= :start and e.date <= :end",
                        Tuple.class)
                .setParameter("category", category)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        if (rows.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = rows.getFirst().get("total", BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getMonth().toString(), b.getCategory(), b.getAmount());
    }
}
