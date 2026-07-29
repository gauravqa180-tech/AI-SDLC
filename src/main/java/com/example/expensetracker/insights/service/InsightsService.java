package com.example.expensetracker.insights.service;

import com.example.expensetracker.insights.api.dto.CategoryTotalResponse;
import com.example.expensetracker.insights.api.dto.InsightsResponse;
import com.example.expensetracker.insights.api.dto.TrendPointResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class InsightsService {

    private final EntityManager em;

    public InsightsService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public InsightsResponse monthInsights(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        YearMonth prev = ym.minusMonths(1);
        LocalDate prevStart = prev.atDay(1);
        LocalDate prevEnd = prev.atEndOfMonth();

        BigDecimal total = sumBetween(start, end);
        BigDecimal prevTotal = sumBetween(prevStart, prevEnd);

        List<CategoryTotalResponse> byCategory = categoryTotalsBetween(start, end);
        List<TrendPointResponse> trend = dailyTotalsBetween(start, end);

        return new InsightsResponse(
                start,
                end,
                total,
                prevTotal,
                total.subtract(prevTotal),
                byCategory,
                trend
        );
    }

    private BigDecimal sumBetween(LocalDate start, LocalDate end) {
        BigDecimal result = em.createQuery(
                        "select coalesce(sum(e.amount), 0) from Expense e " +
                                "where e.deleted = false and e.date >= :start and e.date <= :end",
                        BigDecimal.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return result == null ? BigDecimal.ZERO : result;
    }

    private List<CategoryTotalResponse> categoryTotalsBetween(LocalDate start, LocalDate end) {
        List<Tuple> rows = em.createQuery(
                        "select e.category as category, coalesce(sum(e.amount), 0) as total from Expense e " +
                                "where e.deleted = false and e.date >= :start and e.date <= :end " +
                                "group by e.category order by total desc",
                        Tuple.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        return rows.stream()
                .map(t -> new CategoryTotalResponse(
                        t.get("category", String.class),
                        t.get("total", BigDecimal.class)
                ))
                .toList();
    }

    private List<TrendPointResponse> dailyTotalsBetween(LocalDate start, LocalDate end) {
        List<Tuple> rows = em.createQuery(
                        "select e.date as d, coalesce(sum(e.amount), 0) as total from Expense e " +
                                "where e.deleted = false and e.date >= :start and e.date <= :end " +
                                "group by e.date order by e.date asc",
                        Tuple.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        return rows.stream()
                .map(t -> new TrendPointResponse(
                        t.get("d", LocalDate.class),
                        t.get("total", BigDecimal.class)
                ))
                .toList();
    }
}
