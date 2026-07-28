package com.aisdlc.expensetracker.report;

import com.aisdlc.expensetracker.expense.Expense;
import com.aisdlc.expensetracker.expense.ExpenseRepository;
import com.aisdlc.expensetracker.expense.dto.ExpenseResponse;
import com.aisdlc.expensetracker.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        DateRange r = DateRange.forMonth(month);
        BigDecimal total = expenseRepository.findAll((root, query, cb) -> cb.between(root.get("date"), r.start(), r.end()))
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlyTotalResponse(month, total);
    }

    @Transactional(readOnly = true)
    public MonthlyCategoryBreakdownResponse monthlyCategoryBreakdown(YearMonth month) {
        DateRange r = DateRange.forMonth(month);

        List<Expense> expenses = expenseRepository.findAll((root, query, cb) -> cb.between(root.get("date"), r.start(), r.end()));

        Map<Long, CategoryAccumulator> acc = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Expense e : expenses) {
            total = total.add(e.getAmount());
            Long categoryId = e.getCategory().getId();
            acc.computeIfAbsent(categoryId, id -> new CategoryAccumulator(e.getCategory().getId(), e.getCategory().getName()))
                    .add(e.getAmount());
        }

        List<CategoryBreakdownItem> byCategory = acc.values().stream()
                .sorted(Comparator.comparing(CategoryAccumulator::total).reversed())
                .map(a -> new CategoryBreakdownItem(a.categoryId(), a.categoryName(), a.total()))
                .toList();

        return new MonthlyCategoryBreakdownResponse(month, total, byCategory);
    }

    @Transactional(readOnly = true)
    public TrendsResponse trends(int months) {
        int m = Math.max(1, Math.min(months, 24));
        YearMonth now = YearMonth.now();

        List<MonthlyTrendItem> items = new ArrayList<>();
        for (int i = m - 1; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            items.add(monthlyTotal(ym).total() == null ? new MonthlyTrendItem(ym, BigDecimal.ZERO) : new MonthlyTrendItem(ym, monthlyTotal(ym).total()));
        }
        return new TrendsResponse(m, items);
    }

    @Transactional(readOnly = true)
    public HighlightsResponse highlights(YearMonth month, int topCategories, int topExpenses) {
        MonthlyCategoryBreakdownResponse breakdown = monthlyCategoryBreakdown(month);
        List<CategoryBreakdownItem> topCats = breakdown.byCategory().stream()
                .limit(Math.max(1, Math.min(topCategories, 20)))
                .toList();

        DateRange r = DateRange.forMonth(month);
        List<ExpenseResponse> largest = expenseRepository.findAll(
                        (root, query, cb) -> cb.between(root.get("date"), r.start(), r.end()),
                        PageRequest.of(0, Math.max(1, Math.min(topExpenses, 50)), Sort.by(Sort.Direction.DESC, "amount").and(Sort.by(Sort.Direction.DESC, "date")))
                ).stream().map(this::toExpenseResponse).toList();

        return new HighlightsResponse(month, topCats, largest);
    }

    private ExpenseResponse toExpenseResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getNote()
        );
    }

    private record DateRange(LocalDate start, LocalDate end) {
        static DateRange forMonth(YearMonth month) {
            return new DateRange(month.atDay(1), month.atEndOfMonth());
        }
    }

    private static final class CategoryAccumulator {
        private final Long categoryId;
        private final String categoryName;
        private BigDecimal total = BigDecimal.ZERO;

        private CategoryAccumulator(Long categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        void add(BigDecimal v) {
            total = total.add(v);
        }

        Long categoryId() { return categoryId; }

        String categoryName() { return categoryName; }

        BigDecimal total() { return total; }
    }
}
