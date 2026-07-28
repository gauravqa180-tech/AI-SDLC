package com.example.expensetracker.budget.service;

import com.example.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.api.dto.CategoryBudgetUpsertRequest;
import com.example.expensetracker.budget.domain.CategoryBudget;
import com.example.expensetracker.budget.domain.MonthlyBudget;
import com.example.expensetracker.budget.repo.CategoryBudgetRepository;
import com.example.expensetracker.budget.repo.MonthlyBudgetRepository;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import com.example.expensetracker.expense.service.ExpenseSpecifications;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

  private final MonthlyBudgetRepository monthlyBudgetRepository;
  private final CategoryBudgetRepository categoryBudgetRepository;
  private final ExpenseRepository expenseRepository;

  @Transactional
  public MonthlyBudget upsertMonthly(BudgetUpsertRequest req) {
    validateMonth(req.month());
    MonthlyBudget b = monthlyBudgetRepository.findByMonth(req.month()).orElseGet(MonthlyBudget::new);
    b.setMonth(req.month());
    b.setAmount(req.amount());
    // reset warnings when budget changes
    b.setWarned80(false);
    b.setWarned100(false);
    return monthlyBudgetRepository.save(b);
  }

  @Transactional
  public CategoryBudget upsertCategory(CategoryBudgetUpsertRequest req) {
    validateMonth(req.month());
    CategoryBudget b = categoryBudgetRepository.findByMonthAndCategory(req.month(), req.category())
        .orElseGet(CategoryBudget::new);
    b.setMonth(req.month());
    b.setCategory(req.category());
    b.setAmount(req.amount());
    b.setWarned80(false);
    b.setWarned100(false);
    return categoryBudgetRepository.save(b);
  }

  @Transactional(readOnly = true)
  public BudgetStatusResponse status(String month) {
    validateMonth(month);

    YearMonth ym = YearMonth.parse(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    BigDecimal totalSpent = expenseRepository.sumBetween(start, end);

    MonthlyBudget monthlyBudget = monthlyBudgetRepository.findByMonth(month).orElse(null);
    BigDecimal totalBudget = monthlyBudget == null ? BigDecimal.ZERO : monthlyBudget.getAmount();
    BigDecimal remaining = totalBudget.subtract(totalSpent);

    // category breakdown for month (reusing simple in-memory reduce for MVP)
    Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateGte(start))
        .and(ExpenseSpecifications.dateLte(end));
    List<Expense> expenses = expenseRepository.findAll(spec);

    List<CategoryBudget> categoryBudgets = categoryBudgetRepository.findByMonth(month);

    List<BudgetStatusResponse.CategoryStatus> categoryStatuses = new ArrayList<>();
    for (CategoryBudget cb : categoryBudgets) {
      BigDecimal spent = expenses.stream()
          .filter(e -> cb.getCategory().equals(e.getCategory()))
          .map(Expense::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      categoryStatuses.add(new BudgetStatusResponse.CategoryStatus(
          cb.getCategory(),
          cb.getAmount(),
          spent,
          cb.getAmount().subtract(spent)
      ));
    }

    return new BudgetStatusResponse(month, totalBudget, totalSpent, remaining, categoryStatuses, List.of());
  }

  /**
   * Evaluate thresholds and persist one-time warning flags.
   * Returns alerts produced in this call.
   */
  @Transactional
  public List<String> evaluateAlerts(String month) {
    validateMonth(month);
    YearMonth ym = YearMonth.parse(month);
    BigDecimal spent = expenseRepository.sumBetween(ym.atDay(1), ym.atEndOfMonth());

    List<String> alerts = new ArrayList<>();

    monthlyBudgetRepository.findByMonth(month).ifPresent(b -> {
      BigDecimal eighty = b.getAmount().multiply(new BigDecimal("0.80"));
      if (!b.isWarned80() && spent.compareTo(eighty) >= 0) {
        b.setWarned80(true);
        alerts.add("MONTHLY_BUDGET_80");
      }
      if (!b.isWarned100() && spent.compareTo(b.getAmount()) >= 0) {
        b.setWarned100(true);
        alerts.add("MONTHLY_BUDGET_100");
      }
    });

    // Category budgets
    // For MVP compute per category via queries in memory from list (could be optimized later)
    YearMonth y = YearMonth.parse(month);
    LocalDate start = y.atDay(1);
    LocalDate end = y.atEndOfMonth();
    List<Expense> expenses = expenseRepository.findAll(
        Specification.where(ExpenseSpecifications.dateGte(start)).and(ExpenseSpecifications.dateLte(end))
    );

    for (CategoryBudget cb : categoryBudgetRepository.findByMonth(month)) {
      BigDecimal categorySpent = expenses.stream()
          .filter(e -> cb.getCategory().equals(e.getCategory()))
          .map(Expense::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal eighty = cb.getAmount().multiply(new BigDecimal("0.80"));
      if (!cb.isWarned80() && categorySpent.compareTo(eighty) >= 0) {
        cb.setWarned80(true);
        alerts.add("CATEGORY_BUDGET_80:" + cb.getCategory());
      }
      if (!cb.isWarned100() && categorySpent.compareTo(cb.getAmount()) >= 0) {
        cb.setWarned100(true);
        alerts.add("CATEGORY_BUDGET_100:" + cb.getCategory());
      }
    }

    return alerts;
  }

  private void validateMonth(String month) {
    try {
      YearMonth.parse(month);
    } catch (Exception e) {
      throw new NoSuchElementException("Invalid month format (expected YYYY-MM): " + month);
    }
  }
}
