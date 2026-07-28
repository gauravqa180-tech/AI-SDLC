package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.MonthlyBreakdownItemResponse;
import com.ai.sdlc.expensetracker.api.dto.MonthlyBreakdownResponse;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EntityManager entityManager;
    private final ExpenseRepository expenseRepository;

    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal total = entityManager.createQuery(
                        "select coalesce(sum(e.amount), 0) from Expense e " +
                                "where e.deletedAt is null and e.expenseDate between :start and :end",
                        BigDecimal.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return total == null ? BigDecimal.ZERO : total;
    }

    public MonthlyBreakdownResponse monthlyBreakdown(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Tuple> rows = entityManager.createQuery(
                        "select e.category as category, coalesce(sum(e.amount), 0) as total " +
                                "from Expense e " +
                                "where e.deletedAt is null and e.expenseDate between :start and :end " +
                                "group by e.category " +
                                "order by total desc",
                        Tuple.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        List<MonthlyBreakdownItemResponse> items = rows.stream()
                .map(t -> new MonthlyBreakdownItemResponse(
                        t.get("category", String.class),
                        t.get("total", BigDecimal.class) == null ? BigDecimal.ZERO : t.get("total", BigDecimal.class)
                ))
                .toList();

        BigDecimal total = items.stream()
                .map(MonthlyBreakdownItemResponse::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyBreakdownResponse(month.toString(), total, items);
    }
}
