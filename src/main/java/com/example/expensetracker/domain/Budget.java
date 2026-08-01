package com.example.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_budget_month", columnNames = {"budget_month", "category"})
        },
        indexes = {
                @Index(name = "idx_budget_month", columnList = "budget_month"),
                @Index(name = "idx_budget_month_category", columnList = "budget_month,category")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stored as String (YYYY-MM) to keep MySQL compatibility simple for fresh implementation.
     */
    @Column(name = "budget_month", nullable = false, length = 7)
    private String month;

    /**
     * null means overall monthly budget
     */
    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    public static String toMonthString(YearMonth ym) {
        return ym.toString();
    }
}
