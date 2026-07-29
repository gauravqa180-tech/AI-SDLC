package com.example.expensetracker.expense.service;

import com.example.expensetracker.budget.api.dto.BudgetAlertResponse;
import com.example.expensetracker.budget.service.BudgetService;
import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import com.example.expensetracker.expense.repo.ExpenseSpecifications;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetService budgetService;
    private final Clock clock;

    public ExpenseService(ExpenseRepository expenseRepository, @Lazy BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.budgetService = budgetService;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        Expense saved = expenseRepository.save(e);
        return toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));

        e.setAmount(req.amount());
        e.setDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());

        return toResponse(expenseRepository.save(e));
    }

    @Transactional
    public ExpenseWithAlertResponse createWithAlert(ExpenseCreateRequest req) {
        ExpenseResponse expense = create(req);
        Optional<BudgetAlertResponse> alert = Optional.ofNullable(budgetService.evaluateAlert());
        return new ExpenseWithAlertResponse(expense, alert);
    }

    @Transactional
    public ExpenseWithAlertResponse updateWithAlert(Long id, ExpenseUpdateRequest req) {
        ExpenseResponse expense = update(id, req);
        Optional<BudgetAlertResponse> alert = Optional.ofNullable(budgetService.evaluateAlert());
        return new ExpenseWithAlertResponse(expense, alert);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> search(LocalDate from,
                                       LocalDate to,
                                       String category,
                                       BigDecimal amountMin,
                                       BigDecimal amountMax,
                                       String q,
                                       Pageable pageable) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.amountMin(amountMin))
                .and(ExpenseSpecifications.amountMax(amountMax))
                .and(ExpenseSpecifications.noteContains(q));

        return expenseRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        return toResponse(e);
    }

    @Transactional
    public void softDelete(Long id) {
        Expense e = expenseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        e.softDelete(Instant.now(clock));
        expenseRepository.save(e);
    }

    @Transactional
    public ExpenseResponse restore(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        e.restore();
        return toResponse(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public BigDecimal totalBetween(LocalDate start, LocalDate end) {
        return expenseRepository.sumAmountBetween(start, end);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory(),
                e.getNote(),
                e.isDeleted(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public record ExpenseWithAlertResponse(ExpenseResponse expense, Optional<BudgetAlertResponse> alert) {
        public ExpenseWithAlertResponse {
            if (expense == null) {
                throw new IllegalArgumentException("expense must not be null");
            }
            alert = alert == null ? Optional.empty() : alert;
        }
    }
}