package com.example.expensetracker.expense.service;

import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.expense.api.dto.*;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
        Expense saved = repository.save(Expense.builder()
                .amount(request.amount())
                .expenseDate(request.date())
                .category(request.category().trim())
                .note(request.note())
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense e = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        e.setAmount(request.amount());
        e.setExpenseDate(request.date());
        e.setCategory(request.category().trim());
        e.setNote(request.note());

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
    public Page<ExpenseResponse> search(LocalDate from, LocalDate to, String category, String q,
                                       Integer page, Integer size, String sortBy, String direction) {
        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? 50 : Math.min(200, Math.max(1, size));

        String sortField = (sortBy == null || sortBy.isBlank()) ? "expenseDate" : sortBy;
        Sort.Direction dir = (direction == null) ? Sort.Direction.DESC : Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(p, s, Sort.by(dir, sortField));

        return repository.search(from, to, normalize(category), normalize(q), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return repository.totalForRange(from, to);
    }

    @Transactional(readOnly = true)
    public ReportResponse report(LocalDate from, LocalDate to) {
        BigDecimal total = repository.totalForRange(from, to);
        List<CategoryTotalResponse> byCategory = repository.totalsByCategory(from, to).stream()
                .map(r -> new CategoryTotalResponse(r.getCategory(), r.getTotal()))
                .toList();
        return new ReportResponse(from, to, total, byCategory);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getNote());
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
