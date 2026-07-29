package com.aisdlc.expensetracker.insights.service;

import com.aisdlc.expensetracker.insights.api.dto.CategoryTotal;
import com.aisdlc.expensetracker.insights.api.dto.DailyTotal;
import com.aisdlc.expensetracker.insights.api.dto.MonthlyInsightsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public MonthlyInsightsResponse monthly(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        BigDecimal total = (BigDecimal) em.createQuery(
                        "select coalesce(sum(e.amount),0) from Expense e where e.deleted=false and e.expenseDate between :from and :to")
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();

        List<Tuple> catRows = em.createQuery(
                        "select e.category as category, coalesce(sum(e.amount),0) as total " +
                                "from Expense e " +
                                "where e.deleted=false and e.expenseDate between :from and :to " +
                                "group by e.category " +
                                "order by total desc", Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        List<CategoryTotal> byCategory = catRows.stream()
                .map(t -> new CategoryTotal(t.get("category", String.class), t.get("total", BigDecimal.class)))
                .toList();

        List<Tuple> dailyRows = em.createQuery(
                        "select e.expenseDate as day, coalesce(sum(e.amount),0) as total " +
                                "from Expense e " +
                                "where e.deleted=false and e.expenseDate between :from and :to " +
                                "group by e.expenseDate " +
                                "order by e.expenseDate asc", Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        List<DailyTotal> daily = dailyRows.stream()
                .map(t -> new DailyTotal(t.get("day", LocalDate.class), t.get("total", BigDecimal.class)))
                .toList();

        return new MonthlyInsightsResponse(month.toString(), total, byCategory, daily);
    }
}
