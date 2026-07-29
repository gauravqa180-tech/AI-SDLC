package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.*;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = Expense.builder()
                .amount(req.amount())
                .expenseDate(req.date())
                .category(req.category().trim())
                .note(req.note())
                .build();
        e = expenseRepository.save(e);
        return toResponse(e);
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(
            String q,
            String category,
            LocalDate from,
            LocalDate to,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String sort,
            int page,
            int size
    ) {
        Sort s = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), s);
        var spec = ExpenseRepository.withFilters(q, category, from, to, minAmount, maxAmount);
        return expenseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public void delete(long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return expenseRepository.sumByDateRange(from, to);
    }

    @Transactional(readOnly = true)
    public InsightsResponse insights(LocalDate from, LocalDate to) {
        BigDecimal total = expenseRepository.sumByDateRange(from, to);
        List<Object[]> rows = expenseRepository.sumByCategory(from, to);

        List<InsightsResponse.CategoryTotal> categories = new ArrayList<>();
        for (Object[] r : rows) {
            String category = (String) r[0];
            BigDecimal catTotal = (BigDecimal) r[1];
            BigDecimal pct = BigDecimal.ZERO;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                pct = catTotal.multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, java.math.RoundingMode.HALF_UP);
            }
            categories.add(new InsightsResponse.CategoryTotal(category, catTotal, pct));
        }
        categories.sort(Comparator.comparing(InsightsResponse.CategoryTotal::total).reversed());
        return new InsightsResponse(from, to, total, categories);
    }

    public void writeCsv(
            PrintWriter writer,
            String q,
            String category,
            LocalDate from,
            LocalDate to,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String sort
    ) {
        writer.println("Amount,Date,Category,Note");

        int page = 0;
        while (true) {
            Page<ExpenseResponse> batch = list(q, category, from, to, minAmount, maxAmount, sort, page, 500);
            for (ExpenseResponse e : batch.getContent()) {
                writer.print(escapeCsv(e.amount().toPlainString()));
                writer.print(',');
                writer.print(escapeCsv(e.date().toString()));
                writer.print(',');
                writer.print(escapeCsv(e.category()));
                writer.print(',');
                writer.println(escapeCsv(e.note() == null ? "" : e.note()));
            }
            if (batch.isLast()) {
                break;
            }
            page++;
        }
        writer.flush();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"");
        String v = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + v + "\"" : v;
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        // supported: date_desc, date_asc, amount_desc, amount_asc
        return switch (sort.trim().toLowerCase()) {
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "expenseDate").and(Sort.by(Sort.Direction.ASC, "id"));
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id"));
            case "amount_asc" -> Sort.by(Sort.Direction.ASC, "amount").and(Sort.by(Sort.Direction.DESC, "expenseDate"));
            case "amount_desc" -> Sort.by(Sort.Direction.DESC, "amount").and(Sort.by(Sort.Direction.DESC, "expenseDate"));
            default -> Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getNote());
    }
}
