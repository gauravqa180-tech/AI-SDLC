package com.aisdlc.expensetracker.expense;

import com.aisdlc.expensetracker.expense.dto.ExpenseQuery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ExpenseSpecifications {

    private ExpenseSpecifications() {}

    public static Specification<Expense> fromQuery(ExpenseQuery q) {
        Specification<Expense> spec = Specification.where(null);

        if (q == null) return spec;

        if (q.q() != null && !q.q().isBlank()) {
            String like = "%" + q.q().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("note")), like));
        }
        if (q.categoryId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), q.categoryId()));
        }
        if (q.startDate() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), q.startDate()));
        }
        if (q.endDate() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), q.endDate()));
        }
        BigDecimal min = q.minAmount();
        if (min != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("amount"), min));
        }
        BigDecimal max = q.maxAmount();
        if (max != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("amount"), max));
        }

        return spec;
    }
}
