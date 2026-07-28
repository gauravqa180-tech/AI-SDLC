package com.ai.sdlc.expensetracker.budget.service;

import com.ai.sdlc.expensetracker.alert.service.BudgetAlertService;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.budget.domain.Budget;
import com.ai.sdlc.expensetracker.budget.repo.BudgetRepository;
import com.ai.sdlc.expensetracker.common.api.ResourceNotFoundException;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetAlertService budgetAlertService;

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest req) {
        Budget b = budgetRepository.findByMonthAndCategory(req.month(), req.category().trim())
                .orElseGet(Budget::new);
        b.setMonth(req.month());
        b.setCategory(req.category().trim());
        b.setAmount(req.amount());

        Budget saved = budgetRepository.save(b);

        // after budget change, re-evaluate alerts for that month/category
        budgetAlertService.evaluateThresholds(YearMonth.parse(saved.getMonth()), saved.getCategory());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listByMonth(YearMonth month) {
        return budgetRepository.findAllByMonth(month.toString()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        Budget b = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        budgetRepository.delete(b);
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponse progress(YearMonth month, String category) {
        Budget b = budgetRepository.findByMonthAndCategory(month.toString(), category.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found for " + month + " / " + category));

        BigDecimal spent = spentFor(month, category);
        BigDecimal remaining = b.getAmount().subtract(spent);
        BigDecimal percentUsed = b.getAmount().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : spent.multiply(BigDecimal.valueOf(100)).divide(b.getAmount(), 2, RoundingMode.HALF_UP);

        return new BudgetProgressResponse(month.toString(), category.trim(), b.getAmount(), spent, remaining, percentUsed);
    }

    public BigDecimal spentFor(YearMonth month, String category) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        Specification<Expense> spec = Specification.where((root, query, cb) -> cb.between(root.get("date"), start, end))
                .and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));

        return expenseRepository.findAll(spec).stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetResponse toResponse(Budget b) {
        return new BudgetResponse(b.getId(), b.getMonth(), b.getCategory(), b.getAmount());
    }
}
