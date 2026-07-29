package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.RecurringRuleRequest;
import com.ai.sdlc.expensetracker.api.dto.RecurringRuleResponse;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.domain.RecurringExpenseRule;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import com.ai.sdlc.expensetracker.repository.RecurringExpenseRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRuleRepository ruleRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional
    public RecurringRuleResponse create(RecurringRuleRequest req) {
        RecurringExpenseRule rule = RecurringExpenseRule.builder()
                .amount(req.amount())
                .category(req.category().trim())
                .note(req.note())
                .frequency(req.frequency())
                .startDate(req.startDate())
                .paused(req.paused())
                .lastGeneratedDate(null)
                .build();
        rule = ruleRepository.save(rule);
        return toResponse(rule);
    }

    @Transactional
    public RecurringRuleResponse update(long id, RecurringRuleRequest req) {
        RecurringExpenseRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recurring rule not found: " + id));

        // Apply to future instances only; we do not touch existing expenses.
        rule.setAmount(req.amount());
        rule.setCategory(req.category().trim());
        rule.setNote(req.note());
        rule.setFrequency(req.frequency());
        rule.setStartDate(req.startDate());
        rule.setPaused(req.paused());
        return toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<RecurringRuleResponse> list() {
        return ruleRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Idempotent generation based on lastGeneratedDate.
     */
    @Transactional
    public int generateUpTo(LocalDate upToDate) {
        int created = 0;
        List<RecurringExpenseRule> rules = ruleRepository.findAll();

        for (RecurringExpenseRule rule : rules) {
            if (rule.isPaused()) continue;

            LocalDate cursor = rule.getLastGeneratedDate();
            if (cursor == null) {
                cursor = rule.getStartDate().minusDays(1);
            }

            LocalDate next = nextDate(rule, cursor);
            while (!next.isAfter(upToDate)) {
                Expense expense = Expense.builder()
                        .amount(rule.getAmount())
                        .expenseDate(next)
                        .category(rule.getCategory())
                        .note(rule.getNote())
                        .build();
                expenseRepository.save(expense);
                created++;

                rule.setLastGeneratedDate(next);
                next = nextDate(rule, next);
            }
        }

        return created;
    }

    private LocalDate nextDate(RecurringExpenseRule rule, LocalDate fromInclusive) {
        return switch (rule.getFrequency()) {
            case WEEKLY -> fromInclusive.plusWeeks(1);
            case MONTHLY -> fromInclusive.plusMonths(1);
        };
    }

    private RecurringRuleResponse toResponse(RecurringExpenseRule r) {
        return new RecurringRuleResponse(
                r.getId(),
                r.getAmount(),
                r.getCategory(),
                r.getNote(),
                r.getFrequency(),
                r.getStartDate(),
                r.isPaused(),
                r.getLastGeneratedDate()
        );
    }
}
