package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseSearchRequest;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> fromSearch(ExpenseSearchRequest req) {
        Specification<Expense> spec = Specification.where(null);

        if (req.startDate() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), req.startDate()));
        }
        if (req.endDate() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), req.endDate()));
        }
        if (req.category() != null && !req.category().isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("category")), req.category().toLowerCase(Locale.ROOT)));
        }
        if (req.minAmount() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), req.minAmount()));
        }
        if (req.maxAmount() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), req.maxAmount()));
        }
        if (req.note() != null && !req.note().isBlank()) {
            String like = "%" + req.note().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("note")), like));
        }

        return spec;
    }
}
