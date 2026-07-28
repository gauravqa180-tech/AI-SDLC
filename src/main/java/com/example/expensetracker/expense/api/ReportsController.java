package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.service.ReportsService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

  private final ReportsService reportsService;

  public record CategoryTotal(String category, BigDecimal total) {}

  public record MonthlyTrendPoint(String month, BigDecimal total) {}

  @GetMapping("/category-breakdown")
  public List<CategoryTotal> categoryBreakdown(@RequestParam String month) {
    return reportsService.categoryBreakdown(month);
  }

  @GetMapping("/trend")
  public List<MonthlyTrendPoint> trend(@RequestParam(defaultValue = "6") int months) {
    return reportsService.trend(months);
  }
}
