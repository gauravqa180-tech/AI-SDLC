package com.ai.sdlc.expensetracker.alert.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "budget_alerts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_alert_month_category_threshold", columnNames = {"month", "category", "threshold"})
        },
        indexes = {
                @Index(name = "idx_alert_month", columnList = "month"),
                @Index(name = "idx_alert_month_category", columnList = "month,category")
        }
)
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stored as YYYY-MM */
    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    /** e.g. 0.80 or 1.00 */
    @Column(name = "threshold", nullable = false, precision = 5, scale = 2)
    private BigDecimal threshold;

    @Column(name = "budget_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "spent_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal spentAmount;
}
