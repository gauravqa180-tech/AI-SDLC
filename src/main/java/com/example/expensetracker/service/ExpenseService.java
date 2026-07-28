package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.api.dto.ExpenseResponse;
import com.example.expensetracker.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = Expense.builder()
                .amount(request.amount())
                .date(request.date())
                .category(request.category().trim())
                .note(request.note())
                .build();

        return ExpenseMapper.toResponse(repository.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        return ExpenseMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request) {
        Expense expense = findOrThrow(id);
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setCategory(request.category().trim());
        expense.setNote(request.note());
        return ExpenseMapper.toResponse(repository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate dateFrom,
                                     LocalDate dateTo,
                                     String category,
                                     String q,
                                     String sortField,
                                     String sortOrder) {

        Specification<Expense> spec = Specification.where(null);

        if (dateFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), dateTo));
        }
        if (category != null && !category.isBlank()) {
            String cat = category.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), cat));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("note")), like));
        }

        Sort sort = toSort(sortField, sortOrder);
        return repository.findAll(spec, sort).stream().map(ExpenseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        Specification<Expense> spec = (root, query, cb) -> cb.between(root.get("date"), from, to);

        return repository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Expense findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
    }

    private Sort toSort(String sortField, String sortOrder) {
        String field = (sortField == null || sortField.isBlank()) ? "date" : sortField.trim();
        if (!field.equals("date") && !field.equals("amount")) {
            field = "date";
        }

        Sort.Direction direction = (sortOrder != null && sortOrder.equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }
}
