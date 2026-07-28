package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.ReportsController.CategoryTotal;
import com.example.expensetracker.expense.api.ReportsController.MonthlyTrendPoint;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportsService {

  private final ExpenseRepository expenseRepository;

  @Transactional(readOnly = true)
  public List<CategoryTotal> categoryBreakdown(String month) {
    YearMonth ym = YearMonth.parse(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateGte(start))
        .and(ExpenseSpecifications.dateLte(end));

    List<Expense> rows = expenseRepository.findAll(spec);

    Map<String, BigDecimal> totals = new LinkedHashMap<>();
    for (Expense e : rows) {
      totals.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
    }

    return totals.entrySet().stream().map(en -> new CategoryTotal(en.getKey(), en.getValue())).toList();
  }

  @Transactional(readOnly = true)
  public List<MonthlyTrendPoint> trend(int months) {
    int n = Math.max(1, Math.min(months, 24));

    YearMonth now = YearMonth.now();
    List<MonthlyTrendPoint> points = new ArrayList<>();

    for (int i = n - 1; i >= 0; i--) {
      YearMonth ym = now.minusMonths(i);
      BigDecimal total = expenseRepository.sumBetween(ym.atDay(1), ym.atEndOfMonth());
      points.add(new MonthlyTrendPoint(ym.toString(), total));
    }

    return points;
  }
}
