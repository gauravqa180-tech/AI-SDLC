package com.kan.expensetracker.expense.service;

import com.kan.expensetracker.category.domain.Category;
import com.kan.expensetracker.category.repo.CategoryRepository;
import com.kan.expensetracker.common.api.NotFoundException;
import com.kan.expensetracker.expense.api.dto.ExpenseRequest;
import com.kan.expensetracker.expense.api.dto.ExpenseSearchRequest;
import com.kan.expensetracker.expense.domain.Expense;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Expense create(ExpenseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));

        Expense expense = Expense.builder()
                .amount(request.amount())
                .expenseDate(request.date())
                .category(category)
                .note(request.note())
                .deleted(false)
                .build();

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        if (expense.isDeleted()) {
            throw new IllegalStateException("Cannot update a deleted expense");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));

        expense.setAmount(request.amount());
        expense.setExpenseDate(request.date());
        expense.setCategory(category);
        expense.setNote(request.note());

        return expenseRepository.save(expense);
    }

    public List<Expense> search(ExpenseSearchRequest req) {
        Sort sort = resolveSort(req);

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(req.from()))
                .and(ExpenseSpecifications.dateTo(req.to()))
                .and(ExpenseSpecifications.categoryId(req.categoryId()))
                .and(ExpenseSpecifications.minAmount(req.minAmount()))
                .and(ExpenseSpecifications.maxAmount(req.maxAmount()))
                .and(ExpenseSpecifications.noteContains(req.q()));

        return expenseRepository.findAll(spec, sort);
    }

    @Transactional
    public void softDelete(long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        if (expense.isDeleted()) return;
        expense.setDeleted(true);
        expense.setDeletedAt(Instant.now());
        expenseRepository.save(expense);
    }

    @Transactional
    public Expense undoDelete(long id, int undoWindowSeconds) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        if (!expense.isDeleted()) return expense;

        if (expense.getDeletedAt() == null) {
            throw new IllegalStateException("Cannot undo delete for this expense");
        }

        Instant deadline = expense.getDeletedAt().plusSeconds(undoWindowSeconds);
        if (Instant.now().isAfter(deadline)) {
            throw new IllegalStateException("Undo window expired");
        }

        expense.setDeleted(false);
        expense.setDeletedAt(null);
        return expenseRepository.save(expense);
    }

    public java.math.BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to));

        return expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private Sort resolveSort(ExpenseSearchRequest req) {
        String sortBy = req.sortBy() == null ? "date" : req.sortBy();
        String sortDir = req.sortDir() == null ? "desc" : req.sortDir();

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = sortBy.equalsIgnoreCase("amount") ? "amount" : "expenseDate";
        // Secondary sort for deterministic output
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }
}
