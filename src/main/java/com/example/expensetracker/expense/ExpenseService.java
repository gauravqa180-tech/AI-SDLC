package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.expense.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    public static final String UNCATEGORIZED = "Uncategorized";

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ExpenseResponse create(ExpenseUpsertRequest request) {
        Expense expense = new Expense();
        apply(request, expense);
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpsertRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        apply(request, expense);
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate from, LocalDate to, Long categoryId, BigDecimal minAmount, BigDecimal maxAmount,
                                     String q, String sortBy, Sort.Direction direction) {

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryId(categoryId))
                .and(ExpenseSpecifications.amountMin(minAmount))
                .and(ExpenseSpecifications.amountMax(maxAmount))
                .and(ExpenseSpecifications.noteContains(q));

        Sort sort = Sort.by(direction == null ? Sort.Direction.DESC : direction,
                normalizeSort(sortBy));

        return expenseRepository.findAll(spec, sort).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        YearMonth m = month == null ? YearMonth.now() : month;
        LocalDate from = m.atDay(1);
        LocalDate to = m.atEndOfMonth();

        BigDecimal total = expenseRepository.findAll(
                        Specification.where(ExpenseSpecifications.dateFrom(from))
                                .and(ExpenseSpecifications.dateTo(to)))
                .stream()
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MonthlyTotalResponse(m, total);
    }

    @Transactional(readOnly = true)
    public MonthlyCategoryBreakdownResponse monthlyBreakdown(YearMonth month) {
        YearMonth m = month == null ? YearMonth.now() : month;
        LocalDate from = m.atDay(1);
        LocalDate to = m.atEndOfMonth();

        List<Expense> expenses = expenseRepository.findAll(
                Specification.where(ExpenseSpecifications.dateFrom(from))
                        .and(ExpenseSpecifications.dateTo(to))
        );

        var totals = expenses.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getCategory() == null ? UNCATEGORIZED : e.getCategory().getName(),
                        java.util.stream.Collectors.mapping(Expense::getAmount,
                                java.util.stream.Collectors.reducing(BigDecimal.ZERO, a -> a == null ? BigDecimal.ZERO : a, BigDecimal::add))
                ));

        BigDecimal overall = totals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownItem> items = totals.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(e -> new CategoryBreakdownItem(e.getKey(), e.getValue()))
                .toList();

        return new MonthlyCategoryBreakdownResponse(m, overall, items);
    }

    private void apply(ExpenseUpsertRequest request, Expense expense) {
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setNote(request.note());

        String categoryName = request.categoryName();
        if (categoryName == null || categoryName.isBlank()) {
            expense.setCategory(null);
            return;
        }

        Category category = categoryRepository.findByNameIgnoreCase(categoryName.trim())
                .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName.trim()).build()));
        expense.setCategory(category);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getCategory() == null ? null : expense.getCategory().getName(),
                expense.getNote()
        );
    }

    private String normalizeSort(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) return "expenseDate";
        return switch (sortBy) {
            case "amount" -> "amount";
            case "expenseDate", "date" -> "expenseDate";
            case "createdAt" -> "createdAt";
            default -> "expenseDate";
        };
    }
}
