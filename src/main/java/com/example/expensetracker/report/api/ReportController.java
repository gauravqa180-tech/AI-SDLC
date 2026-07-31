package com.example.expensetracker.report.api;

import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ExpenseRepository expenseRepository;

    @GetMapping("/monthly-total")
    public BigDecimal monthlyTotal(@RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return expenseRepository.sumAmountBetween(start, end);
    }
}
