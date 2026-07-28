package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repo.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ExpenseRepository expenseRepository;
    private final BudgetAlertService budgetAlertService;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(req.amount())
                .date(req.date())
                .category(req.category().trim())
                .note(req.note())
                .build());

        budgetAlertService.evaluateAndCreateAlerts(saved.getDate());

        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        Expense saved = expenseRepository.save(e);
        budgetAlertService.evaluateAndCreateAlerts(saved.getDate());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to, String category, BigDecimal minAmount, BigDecimal maxAmount, String q, Sort sort) {
        Specification<Expense> spec = Specification.where(null);
        if (from != null && to != null) {
            spec = spec.and(ExpenseSpecifications.dateBetween(from, to));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and(ExpenseSpecifications.categoryEquals(category));
        }
        if (minAmount != null) {
            spec = spec.and(ExpenseSpecifications.amountGte(minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and(ExpenseSpecifications.amountLte(maxAmount));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(ExpenseSpecifications.noteContains(q));
        }

        return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        BigDecimal total = expenseRepository.sumAmountBetween(from, to);
        return new MonthlyTotalResponse(month.toString(), total);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        BigDecimal total = expenseRepository.sumAmountBetween(from, to);

        List<Object[]> rows = expenseRepository.sumByCategoryBetween(from, to);
        List<CategoryTotalResponse> byCategory = new ArrayList<>();
        for (Object[] r : rows) {
            String cat = Objects.toString(r[0], null);
            BigDecimal catTotal = (BigDecimal) r[1];
            double pct = total.signum() == 0 ? 0.0 : catTotal.divide(total, 6, BigDecimal.ROUND_HALF_UP).doubleValue();
            byCategory.add(new CategoryTotalResponse(cat, catTotal, pct));
        }

        return new MonthlySummaryResponse(month.toString(), total, byCategory);
    }

    public void writeCsv(List<ExpenseResponse> expenses, PrintWriter writer) {
        writer.println("id,date,amount,category,note");
        for (ExpenseResponse e : expenses) {
            writer.print(e.id());
            writer.print(',');
            writer.print(ISO_DATE.format(e.date()));
            writer.print(',');
            writer.print(e.amount().setScale(2));
            writer.print(',');
            writer.print(csvEscape(e.category()));
            writer.print(',');
            writer.print(csvEscape(e.note()));
            writer.println();
        }
    }

    private String csvEscape(String v) {
        if (v == null) {
            return "";
        }
        boolean mustQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        String escaped = v.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
