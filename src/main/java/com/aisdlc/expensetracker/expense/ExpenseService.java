package com.aisdlc.expensetracker.expense;

import com.aisdlc.expensetracker.category.Category;
import com.aisdlc.expensetracker.category.CategoryService;
import com.aisdlc.expensetracker.expense.dto.ExpenseQuery;
import com.aisdlc.expensetracker.expense.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.dto.ExpenseUpsertRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;

    @Transactional
    public ExpenseResponse create(ExpenseUpsertRequest req) {
        Category category = categoryService.getEntity(req.categoryId());
        Expense saved = expenseRepository.save(Expense.builder()
                .amount(req.amount())
                .date(req.date())
                .category(category)
                .note(req.note() == null ? null : req.note().trim())
                .build());
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpsertRequest req) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));

        Category category = categoryService.getEntity(req.categoryId());
        expense.setAmount(req.amount());
        expense.setDate(req.date());
        expense.setCategory(category);
        expense.setNote(req.note() == null ? null : req.note().trim());

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(ExpenseQuery q) {
        Specification<Expense> spec = ExpenseSpecifications.fromQuery(q);
        Sort sort = parseSort(q == null ? null : q.sort());
        return expenseRepository.findAll(spec, sort).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return switch (sort.trim()) {
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "id"));
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
            case "amount_asc" -> Sort.by(Sort.Direction.ASC, "amount").and(Sort.by(Sort.Direction.DESC, "date"));
            case "amount_desc" -> Sort.by(Sort.Direction.DESC, "amount").and(Sort.by(Sort.Direction.DESC, "date"));
            default -> throw new IllegalArgumentException("Unsupported sort: " + sort + ". Use date_asc|date_desc|amount_asc|amount_desc");
        };
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getNote()
        );
    }
}
