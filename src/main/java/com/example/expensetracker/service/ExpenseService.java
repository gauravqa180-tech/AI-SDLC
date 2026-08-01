package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense saved = expenseRepository.save(toEntity(null, request));
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setCategory(request.category());
        existing.setNote(request.note());
        return toResponse(expenseRepository.save(existing));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to, String category,
                                     BigDecimal minAmount, BigDecimal maxAmount,
                                     String noteKeyword,
                                     String sortBy, String sortOrder) {

        Specification<com.example.expensetracker.domain.Expense> spec = Specification.where(ExpenseRepository.dateGte(from))
                .and(ExpenseRepository.dateLte(to))
                .and(ExpenseRepository.categoryEq(category))
                .and(ExpenseRepository.amountGte(minAmount))
                .and(ExpenseRepository.amountLte(maxAmount))
                .and(ExpenseRepository.noteContains(noteKeyword));

        Sort sort = toSort(sortBy, sortOrder);
        return expenseRepository.findAll(spec, sort).stream().map(ExpenseService::toResponse).toList();
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
        BigDecimal total = expenseRepository.sumByDateRange(from, to);
        return new MonthlyTotalResponse(month.toString(), total);
    }

    @Transactional(readOnly = true)
    public MonthlyInsightsResponse monthlyInsights(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        BigDecimal total = expenseRepository.sumByDateRange(from, to);

        List<Object[]> rows = expenseRepository.sumByCategoryInDateRange(from, to);
        List<CategoryBreakdownItem> items = rows.stream()
                .map(r -> {
                    String category = (String) r[0];
                    BigDecimal catTotal = (BigDecimal) r[1];
                    BigDecimal pct = total.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : catTotal.multiply(BigDecimal.valueOf(100))
                            .divide(total, 2, RoundingMode.HALF_UP);
                    return new CategoryBreakdownItem(category, catTotal, pct);
                })
                .sorted(Comparator.comparing(CategoryBreakdownItem::total).reversed())
                .toList();

        return new MonthlyInsightsResponse(month.toString(), total, items);
    }

    private static Sort toSort(String sortBy, String sortOrder) {
        String normalizedSortBy = (sortBy == null || sortBy.isBlank()) ? "date" : sortBy;
        String normalizedOrder = (sortOrder == null || sortOrder.isBlank()) ? "desc" : sortOrder;

        Sort.Direction direction = normalizedOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        if (normalizedSortBy.equalsIgnoreCase("amount")) {
            return Sort.by(direction, "amount");
        }
        return Sort.by(direction, "date");
    }

    private static Expense toEntity(Long id, ExpenseRequest req) {
        return Expense.builder()
                .id(id)
                .amount(req.amount())
                .date(req.date())
                .category(req.category())
                .note(req.note())
                .build();
    }

    private static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }
}
