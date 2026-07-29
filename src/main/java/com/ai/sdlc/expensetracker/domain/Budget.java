package com.ai.sdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_period_category", columnNames = {"period", "category"})
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
     * Stored as YYYY-MM for simplicity.
     */
    @Column(nullable = false, length = 7)
    private String period;

    /**
     * Null/blank means overall budget.
     */
    @Column(length = 100)
    private String category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal warnThresholdPercent;

    public static String periodOf(YearMonth ym) {
        return ym.toString();
    }
}
