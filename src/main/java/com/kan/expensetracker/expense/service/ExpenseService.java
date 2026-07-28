package com.kan.expensetracker.expense.service;

import com.kan.expensetracker.common.api.NotFoundException;
import com.kan.expensetracker.expense.api.dto.ExpenseRequest;
import com.kan.expensetracker.expense.api.dto.ExpenseResponse;
import com.kan.expensetracker.expense.domain.Expense;
import com.kan.expensetracker.expense.repo.ExpenseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest req) {
        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        Expense saved = repository.save(e);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(long id) {
        Expense e = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        return toResponse(e);
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseRequest req) {
        Expense e = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        return toResponse(repository.save(e));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> search(String q, String category, LocalDate from, LocalDate to, String sort) {
        Specification<Expense> spec = Specification.where(null);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("note")), like));
        }

        if (category != null && !category.isBlank()) {
            String cat = category.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), cat));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to));
        }

        Sort s = parseSort(sort);
        return repository.findAll(spec, s).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);
        return repository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Sort parseSort(String sort) {
        // Supported: date_desc (default), date_asc, amount_desc, amount_asc
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return switch (sort.trim().toLowerCase()) {
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "id"));
            case "amount_desc" -> Sort.by(Sort.Direction.DESC, "amount").and(Sort.by(Sort.Direction.DESC, "id"));
            case "amount_asc" -> Sort.by(Sort.Direction.ASC, "amount").and(Sort.by(Sort.Direction.ASC, "id"));
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
            default -> Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        };
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory(),
                e.getNote(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
