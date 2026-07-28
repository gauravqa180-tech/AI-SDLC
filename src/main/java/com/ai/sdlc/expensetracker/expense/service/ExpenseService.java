package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.alert.service.BudgetAlertService;
import com.ai.sdlc.expensetracker.common.api.ResourceNotFoundException;
import com.ai.sdlc.expensetracker.expense.api.dto.*;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetAlertService budgetAlertService;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        Expense saved = expenseRepository.save(e);

        budgetAlertService.evaluateThresholds(YearMonth.from(saved.getDate()), saved.getCategory());

        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        budgetAlertService.evaluateThresholds(YearMonth.from(e.getDate()), e.getCategory());

        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return expenseRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> search(ExpenseSearchRequest req) {
        Specification<Expense> spec = ExpenseSpecifications.fromSearch(req);
        Sort sort = buildSort(req.sortBy(), req.sortDir());
        return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        var spec = Specification.<Expense>where((root, query, cb) -> cb.between(root.get("date"), start, end));
        var total = expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new MonthlyTotalResponse(month.toString(), total);
    }

    public record MonthlyTotalResponse(String month, java.math.BigDecimal total) {
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String by = (sortBy == null || sortBy.isBlank()) ? "date" : sortBy;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        if (!by.equals("date") && !by.equals("amount")) {
            by = "date";
        }
        return Sort.by(dir, by);
    }
}
