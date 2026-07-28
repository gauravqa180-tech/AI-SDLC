package com.ai.sdlc.expensetracker.reports.service;

import com.ai.sdlc.expensetracker.expenses.domain.Expense;
import com.ai.sdlc.expensetracker.expenses.repo.ExpenseRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class ReportService {

    private final ExpenseRepository repository;

    public ReportService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);
        return repository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
