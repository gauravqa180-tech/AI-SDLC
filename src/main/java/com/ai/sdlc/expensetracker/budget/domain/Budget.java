package com.ai.sdlc.expensetracker.budget.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@Entity
@Table(name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"month", "category"})
        },
        indexes = {
                @Index(name = "idx_budget_month", columnList = "month"),
                @Index(name = "idx_budget_month_category", columnList = "month,category")
        }
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stored as YYYY-MM */
    @Column(name = "month", nullable = false, length = 7)
    private String month;

    /** Optional overall budget if null/blank; for simplicity we implement per-category budgets only */
    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public static String toDbMonth(YearMonth ym) {
        return ym.toString();
    }
}
