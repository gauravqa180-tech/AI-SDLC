package com.example.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_budgets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"})
        },
        indexes = {
                @Index(name = "idx_budget_month", columnList = "budget_month"),
                @Index(name = "idx_budget_category", columnList = "category")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored as yyyy-MM for easy querying.
     */
    @Column(name = "budget_month", nullable = false, length = 7)
    private String month;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public static String toKey(YearMonth ym) {
        return ym.toString(); // yyyy-MM
    }
}
