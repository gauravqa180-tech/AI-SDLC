package com.kan.expensetracker.insights.service;

import com.kan.expensetracker.expense.domain.Expense;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import com.kan.expensetracker.expense.service.ExpenseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final ExpenseRepository expenseRepository;

    public Map<String, BigDecimal> monthlyByCategory(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Expense e : expenseRepository.findAll(spec)) {
            String categoryName = e.getCategory().getName();
            result.put(categoryName, result.getOrDefault(categoryName, BigDecimal.ZERO).add(e.getAmount()));
        }
        return result;
    }
}
