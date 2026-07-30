package com.ai.sdlc.expensetracker.budget.service;

import com.ai.sdlc.expensetracker.budget.api.dto.*;
import com.ai.sdlc.expensetracker.budget.domain.Budget;
import com.ai.sdlc.expensetracker.budget.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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
  public BudgetResponse upsert(BudgetUpsertRequest req) {
    YearMonth ym = YearMonth.parse(req.month());
    String category = (req.category() == null || req.category().isBlank()) ? null : req.category();

    Budget b = budgetRepository.findByMonthAndCategory(ym, category)
        .orElseGet(() -> Budget.builder().month(ym).category(category).build());

    b.setAmount(req.amount());
    Budget saved = budgetRepository.save(b);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<BudgetResponse> list(String month) {
    YearMonth ym = YearMonth.parse(month);
    return budgetRepository.findAllByMonth(ym).stream().map(this::toResponse).toList();
  }

  @Transactional
  public void delete(long id) {
    budgetRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<BudgetAlertResponse> alerts(String month) {
    YearMonth ym = YearMonth.parse(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    List<BudgetAlertResponse> result = new ArrayList<>();

    // overall
    budgetRepository.findByMonthAndCategoryIsNull(ym).ifPresent(b -> {
      BigDecimal spend = expenseRepository.sumAmountBetween(start, end);
      result.addAll(buildAlerts(ym, null, spend, b.getAmount()));
    });

    // per-category
    for (Budget b : budgetRepository.findAllByMonth(ym)) {
      if (b.getCategory() == null) continue;
      BigDecimal spend = expenseRepository.search(start, end, b.getCategory(), null, org.springframework.data.domain.Sort.unsorted())
          .stream()
          .map(e -> e.getAmount())
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      result.addAll(buildAlerts(ym, b.getCategory(), spend, b.getAmount()));
    }

    return result;
  }

  private List<BudgetAlertResponse> buildAlerts(YearMonth ym, String category, BigDecimal spend, BigDecimal budget) {
    List<BudgetAlertResponse> alerts = new ArrayList<>();
    if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) return alerts;

    BigDecimal pct = spend.multiply(BigDecimal.valueOf(100)).divide(budget, 2, java.math.RoundingMode.HALF_UP);
    if (pct.compareTo(BigDecimal.valueOf(80)) >= 0) {
      alerts.add(new BudgetAlertResponse(
          ym.toString(), category, spend, budget, 80,
          message(category, spend, budget, 80)
      ));
    }
    if (pct.compareTo(BigDecimal.valueOf(100)) >= 0) {
      alerts.add(new BudgetAlertResponse(
          ym.toString(), category, spend, budget, 100,
          message(category, spend, budget, 100)
      ));
    }
    return alerts;
  }

  private String message(String category, BigDecimal spend, BigDecimal budget, int threshold) {
    String target = (category == null) ? "monthly" : ("'" + category + "' category");
    return "You have reached " + threshold + "% of your " + target + " budget. Spend=" + spend + ", Budget=" + budget;
  }

  private BudgetResponse toResponse(Budget b) {
    return new BudgetResponse(b.getId(), b.getMonth().toString(), b.getCategory(), b.getAmount());
  }
}
