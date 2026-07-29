package com.example.expensetracker.budget.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets",
        uniqueConstraints = @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"}),
        indexes = {
                @Index(name = "idx_budget_month", columnList = "budget_month"),
                @Index(name = "idx_budget_category", columnList = "category")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored as YYYY-MM (e.g., 2026-05).
     */
    @Column(name = "budget_month", nullable = false, length = 7)
    private String month;

    /**
     * Nullable: null category means overall monthly budget.
     */
    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public YearMonth getMonthAsYearMonth() {
        return YearMonth.parse(month);
    }
}
