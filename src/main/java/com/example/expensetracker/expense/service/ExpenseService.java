package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.*;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        e = expenseRepository.save(e);
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to, String category, String q, String sortBy, String sortDir) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.noteContains(q));

        Sort sort = buildSort(sortBy, sortDir);
        return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return expenseRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));

        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        return toResponse(expenseRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Expense not found");
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        BigDecimal total = expenseRepository.findAll(
                        Specification.where(ExpenseSpecifications.dateFrom(from)).and(ExpenseSpecifications.dateTo(to))
                ).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyTotalResponse(month, total);
    }

    @Transactional(readOnly = true)
    public MonthlyCategoryBreakdownResponse monthlyCategoryBreakdown(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findAll(
                Specification.where(ExpenseSpecifications.dateFrom(from)).and(ExpenseSpecifications.dateTo(to))
        );

        Map<String, BigDecimal> totalsByCategory = expenses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Expense::getCategory,
                        java.util.stream.Collectors.mapping(Expense::getAmount,
                                java.util.stream.Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        BigDecimal monthlyTotal = totalsByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownItem> breakdown = totalsByCategory.entrySet().stream()
                .map(e -> new CategoryBreakdownItem(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CategoryBreakdownItem::total).reversed())
                .toList();

        List<CategoryBreakdownItem> top3 = breakdown.stream().limit(3).toList();

        return new MonthlyCategoryBreakdownResponse(month, monthlyTotal, breakdown, top3);
    }

    public void writeCsv(List<ExpenseResponse> expenses, PrintWriter writer) {
        writer.println("Date,Amount,Category,Note");
        for (ExpenseResponse e : expenses) {
            writer.print(csv(e.date().toString()));
            writer.print(',');
            writer.print(csv(e.amount().toPlainString()));
            writer.print(',');
            writer.print(csv(e.category()));
            writer.print(',');
            writer.println(csv(e.note() == null ? "" : e.note()));
        }
        writer.flush();
    }

    private String csv(String val) {
        String v = val == null ? "" : val;
        boolean needsQuotes = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (needsQuotes) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String s = (sortBy == null || sortBy.isBlank()) ? "date" : sortBy;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // allow only known sort fields
        if (!List.of("date", "amount", "category").contains(s)) {
            s = "date";
        }
        return Sort.by(dir, s);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
