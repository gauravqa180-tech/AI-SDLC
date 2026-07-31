package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import com.example.expensetracker.category.CategoryRepository;
import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.expense.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.dto.ExpenseUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public Expense create(ExpenseCreateRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));

        Expense expense = Expense.builder()
                .amount(req.amount())
                .expenseDate(req.expenseDate())
                .category(category)
                .note(req.note())
                .build();
        return expenseRepository.save(expense);
    }

    public Expense update(long id, ExpenseUpdateRequest req) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + req.categoryId()));

        expense.setAmount(req.amount());
        expense.setExpenseDate(req.expenseDate());
        expense.setCategory(category);
        expense.setNote(req.note());

        return expenseRepository.save(expense);
    }

    @Transactional
    public void softDelete(long id) {
        Expense expense = expenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        expense.setDeletedAt(OffsetDateTime.now());
        expenseRepository.save(expense);
    }

    @Transactional
    public void undoDelete(long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        expense.setDeletedAt(null);
        expenseRepository.save(expense);
    }

    public List<Expense> list(LocalDate start, LocalDate end,
                              Long categoryId,
                              BigDecimal minAmount,
                              BigDecimal maxAmount,
                              String q,
                              Sort sort) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateBetween(start, end));

        if (categoryId != null) {
            spec = spec.and(ExpenseSpecifications.categoryIdEquals(categoryId));
        }
        if (minAmount != null) {
            spec = spec.and(ExpenseSpecifications.amountGte(minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and(ExpenseSpecifications.amountLte(maxAmount));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(ExpenseSpecifications.noteContains(q.trim()));
        }

        return expenseRepository.findAll(spec, sort);
    }

    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return expenseRepository.sumForDateRange(start, end);
    }
}
