package com.ai.sdlc.expensetracker.summary.service;

import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import com.ai.sdlc.expensetracker.summary.api.dto.MonthlyCategoryTotalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlySummaryService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public MonthlyCategoryTotalResponse categoryTotals(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), start, end);

        var expenses = expenseRepository.findAll(spec);
        Map<String, BigDecimal> totals = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() == null ? "" : e.getCategory(),
                        Collectors.mapping(Expense::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        BigDecimal monthlyTotal = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        var categories = totals.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isBlank())
                .map(e -> new MonthlyCategoryTotalResponse.CategoryTotal(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MonthlyCategoryTotalResponse.CategoryTotal::total).reversed())
                .toList();

        return new MonthlyCategoryTotalResponse(month.toString(), monthlyTotal, categories);
    }
}
