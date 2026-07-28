package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.domain.Budget;
import com.ai.sdlc.expensetracker.repo.BudgetRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional
    public BudgetResponse upsert(BudgetUpsertRequest req) {
        String category = normalizeCategory(req.category());
        Budget budget = budgetRepository.findByMonthAndCategory(req.month(), category)
                .orElse(Budget.builder().month(req.month()).category(category).build());

        if (category != null && category.isBlank()) {
            throw new ValidationException("Category cannot be blank");
        }

        budget.setAmount(req.amount());
        Budget saved = budgetRepository.save(budget);
        return new BudgetResponse(saved.getId(), saved.getMonth(), saved.getCategory(), saved.getAmount());
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> listByMonth(String month) {
        return budgetRepository.findByMonth(month).stream()
                .map(b -> new BudgetResponse(b.getId(), b.getMonth(), b.getCategory(), b.getAmount()))
                .toList();
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
