package com.ai.sdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_budgets_month_category", columnNames = {"budget_month", "category"})
        },
        indexes = {
                @Index(name = "idx_budgets_month", columnList = "budget_month")
        }
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored as YYYY-MM (e.g., 2026-07).
     */
    @Column(name = "budget_month", nullable = false, length = 7)
    private String month;

    /**
     * Null category means overall monthly budget.
     */
    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public static String toMonthString(YearMonth ym) {
        return ym.toString();
    }
}
