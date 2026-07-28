package com.aisdlc.expensetracker.budget;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "budget_alerts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_alert_month_type_threshold", columnNames = {"month", "type", "category_id", "threshold"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "month", nullable = false, length = 7)
    private String month; // yyyy-MM

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private BudgetType type;

    @Column(name = "category_id")
    private Long categoryId; // nullable for OVERALL

    @Column(name = "threshold", nullable = false)
    private int threshold; // 80 or 100

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged;
}
