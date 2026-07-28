package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.BudgetProgressResponse;
import com.example.expensetracker.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.domain.Budget;
import com.example.expensetracker.repo.BudgetRepository;
import com.example.expensetracker.repo.ExpenseRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetService {

  private final BudgetRepository budgetRepository;
  private final ExpenseRepository expenseRepository;

  @Transactional
  public BudgetProgressResponse upsert(BudgetUpsertRequest req) {
    String month = req.month();
    String category = normalizeCategory(req.category());

    Budget budget = budgetRepository.findByMonthAndCategory(month, category)
        .orElseGet(() -> Budget.builder().month(month).category(category).build());

    budget.setAmount(req.amount());
    Budget saved = budgetRepository.save(budget);
    return progress(saved);
  }

  @Transactional
  public void delete(long id) {
    if (!budgetRepository.existsById(id)) {
      throw new NotFoundException("Budget not found: " + id);
    }
    budgetRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<BudgetProgressResponse> progressForMonth(String month) {
    return budgetRepository.findByMonth(month).stream().map(this::progress).toList();
  }

  private BudgetProgressResponse progress(Budget budget) {
    YearMonth ym = YearMonth.parse(budget.getMonth());
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    BigDecimal spent;
    if (budget.getCategory() == null) {
      spent = expenseRepository.sumAmountBetween(start, end);
    } else {
      spent = expenseRepository.sumByCategoryBetween(start, end).stream()
          .filter(p -> budget.getCategory().equals(p.getCategory()))
          .findFirst()
          .map(ExpenseRepository.CategoryTotalProjection::getTotal)
          .orElse(BigDecimal.ZERO);
    }

    BigDecimal budgetAmount = budget.getAmount();
    BigDecimal percentUsed = BigDecimal.ZERO;
    if (budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
      percentUsed = spent
          .multiply(BigDecimal.valueOf(100))
          .divide(budgetAmount, 2, RoundingMode.HALF_UP);
    }

    boolean overspent = budgetAmount != null && spent.compareTo(budgetAmount) > 0;

    return new BudgetProgressResponse(
        budget.getId(),
        budget.getMonth(),
        budget.getCategory(),
        budgetAmount,
        spent,
        percentUsed,
        overspent
    );
  }

  private String normalizeCategory(String category) {
    if (category == null) return null;
    String trimmed = category.trim();
    return trimmed.isBlank() ? null : trimmed;
  }
}
