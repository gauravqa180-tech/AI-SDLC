package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.domain.Expense;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    static Specification<Expense> build(LocalDate startDate,
                                       LocalDate endDate,
                                       String category,
                                       BigDecimal minAmount,
                                       BigDecimal maxAmount,
                                       String keyword) {
        Specification<Expense> spec = Specification.where(null);

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
        }
        if (category != null && !category.isBlank()) {
            String normalized = category.trim();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), normalized));
        }
        if (minAmount != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("note")), like));
        }

        return spec;
    }
}
